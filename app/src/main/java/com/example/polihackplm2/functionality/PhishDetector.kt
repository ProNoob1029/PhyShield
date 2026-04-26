package com.example.polihackplm2.functionality

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import com.example.polihackplm2.network.urlscan.ScanResult
import com.example.polihackplm2.network.urlscan.UrlScanServiceImpl
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object PhishDetector {
    data class BoundingBox(
        val ymin: Int,
        val xmin: Int,
        val ymax: Int,
        val xmax: Int,
        val label: String
    )

    data class PhishResult(
        val isMalicious: Boolean,
        val url: String,
        val reasons: List<String> = emptyList(),
        val detectedBy: String = "",
        val screenshot: Bitmap? = null,
        val confidenceScore: Int = 0,
        val boundingBoxes: List<BoundingBox> = emptyList(),
        val id: Int = 0
    )

    sealed class ScanState {
        object Loading : ScanState()
        data class InterimAiResult(val result: PhishResult) : ScanState()
        data class FinalResult(val result: PhishResult) : ScanState()
    }

    private const val MOBILE_USER_AGENT = "Mozilla/5.0 (Android 16; Mobile; rv:146.0) Gecko/146.0 Firefox/146.0"

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-3.1-flash-lite-preview",
    )
    
    private val client = OkHttpClient()

    private val urlScanService = UrlScanServiceImpl(client)

    private suspend fun getSensitivityGuidance(context: Context): String {
        val sensitivity = SettingsManager.getSensitivity(context).first()
        return when(sensitivity) {
            0 -> "Be cautious: only flag if you are highly certain it is a phishing link."
            1 -> "Be balanced: standard security analysis for phishing detection."
            else -> "Be aggressive: flag even if there are subtle suspicious patterns or unknown origin."
        }
    }

    /**
     * Progressive check function:
     * 1. Returns a flow that emits states.
     * 2. Immediately starts AI analysis on the URL text and emits InterimAiResult.
     * 3. Concurrently starts urlscan.io request.
     * 4. When urlscan finishes, emits FinalResult with screenshot if available.
     */
    fun checkUrlFlow(context: Context, url: String): Flow<ScanState> = flow {
        emit(ScanState.Loading)

        // Step 0: Check Local Blocklist (User enforced)
        if (BlocklistManager.isBlocked(url)) {
            emit(ScanState.FinalResult(
                PhishResult(
                    isMalicious = true,
                    url = url,
                    reasons = listOf("This site was previously blocked and reported by you"),
                    detectedBy = "User Blocklist"
                )
            ))
            return@flow
        }

        val sensitivityGuidance = getSensitivityGuidance(context)

        coroutineScope {
            // Start AI analysis concurrently based on just the URL text
            val aiDeferred = async {
                val prompt = """
                    Analyze the following URL for phishing or malicious intent: $url
                    Guidance: $sensitivityGuidance
                    Respond in the following JSON format ONLY:
                    {
                      "isMalicious": boolean,
                      "confidenceScore": integer (0-100),
                      "reasons": ["reason 1", "reason 2"]
                    }
                """.trimIndent()

                try {
                    val response = generativeModel.generateContent(prompt)
                    val text = response.text ?: ""
                    
                    val startJson = text.indexOf("{")
                    val endJson = text.lastIndexOf("}")
                    if (startJson == -1 || endJson == -1) {
                        checkUrlLocally(url).copy(detectedBy = "Local Analysis")
                    } else {
                        val jsonStr = text.substring(startJson, endJson + 1)
                        val json = JSONObject(jsonStr)
                        val isMalicious = json.optBoolean("isMalicious", false)
                        val confidenceScore = json.optInt("confidenceScore", if (isMalicious) 60 else 0)
                        val reasons = mutableListOf<String>()
                        val reasonsArray = json.optJSONArray("reasons")
                        if (reasonsArray != null) {
                            for (i in 0 until reasonsArray.length()) {
                                reasons.add(reasonsArray.getString(i))
                            }
                        }
                        PhishResult(isMalicious, url, reasons, if (isMalicious) "Gemini AI (Interim)" else "Gemini AI (Interim)", confidenceScore = confidenceScore)
                    }
                } catch (e: Exception) {
                    Log.e("PhishDetector", "Interim AI analysis failed", e)
                    checkUrlLocally(url).copy(detectedBy = "Local Analysis (Offline)")
                }
            }

            // Start UrlScan concurrently
            val urlScanDeferred = async {
                var scanResult: ScanResult? = null
                var screenshotBitmap: Bitmap? = null
                try {
                    val submission = urlScanService.submitUrl(url, customAgent = MOBILE_USER_AGENT)
                    scanResult = urlScanService.waitForScanResult(submission.uuid, timeoutMillis = 30_000)
                    
                    if (scanResult != null) {
                        screenshotBitmap = downloadBitmap(scanResult.screenshotUrl)
                    }
                } catch (e: Exception) {
                    Log.e("PhishDetector", "UrlScan failed", e)
                }
                Pair(scanResult, screenshotBitmap)
            }

            // Wait for interim AI result and emit it
            val interimResult = aiDeferred.await()
            emit(ScanState.InterimAiResult(interimResult))

            // Wait for UrlScan result
            val (scanResult, screenshotBitmap) = urlScanDeferred.await()

            if (scanResult != null && scanResult.malicious) {
                val reasons = mutableListOf("Flagged as malicious by urlscan.io community", "Risk score: ${scanResult.score}/100")
                reasons.addAll(scanResult.tags)
                emit(ScanState.FinalResult(
                    PhishResult(
                        isMalicious = true,
                        url = url,
                        reasons = reasons,
                        detectedBy = "urlscan.io",
                        screenshot = screenshotBitmap,
                        confidenceScore = scanResult.score
                    )
                ))
                return@coroutineScope
            }

            // If UrlScan didn't definitively flag it as malicious, check if we need to refine with screenshot
            if (screenshotBitmap != null) {
                 val promptWithScreenshot = """
                    Analyze the following URL for phishing or malicious intent: $url
                    Guidance: $sensitivityGuidance
                    
                    Identify any suspicious elements on the page (e.g., login forms, deceptive buttons, urgent warnings, fake brand logos).
                    
                    Respond in the following JSON format ONLY:
                    {
                      "isMalicious": boolean,
                      "confidenceScore": integer (0-100),
                      "reasons": ["reason 1", "reason 2"],
                      "boundingBoxes": [
                        {"ymin": int, "xmin": int, "ymax": int, "xmax": int, "label": "string"}
                      ]
                    }
                    Note: Bounding boxes use 0-1000 normalized coordinates.
                """.trimIndent()
                
                try {
                    val finalResponse = generativeModel.generateContent(
                        content {
                            image(screenshotBitmap)
                            text("$promptWithScreenshot\nAlso analyze the provided screenshot of the page.")
                        }
                    )
                    val text = finalResponse.text ?: ""
                    
                    val startJson = text.indexOf("{")
                    val endJson = text.lastIndexOf("}")
                    if (startJson == -1 || endJson == -1) {
                         emit(ScanState.FinalResult(interimResult.copy(detectedBy = interimResult.detectedBy.replace(" (Interim)", ""), screenshot = screenshotBitmap)))
                    } else {
                        val jsonStr = text.substring(startJson, endJson + 1)
                        val json = JSONObject(jsonStr)
                        val isMalicious = json.optBoolean("isMalicious", false)
                        val confidenceScore = json.optInt("confidenceScore", if (isMalicious) 85 else 0)
                        val reasons = mutableListOf<String>()
                        val reasonsArray = json.optJSONArray("reasons")
                        if (reasonsArray != null) {
                            for (i in 0 until reasonsArray.length()) {
                                reasons.add(reasonsArray.getString(i))
                            }
                        }

                        val boundingBoxes = mutableListOf<BoundingBox>()
                        val boxesArray = json.optJSONArray("boundingBoxes")
                        if (boxesArray != null) {
                            for (i in 0 until boxesArray.length()) {
                                val boxJson = boxesArray.getJSONObject(i)
                                boundingBoxes.add(
                                    BoundingBox(
                                        boxJson.getInt("ymin"),
                                        boxJson.getInt("xmin"),
                                        boxJson.getInt("ymax"),
                                        boxJson.getInt("xmax"),
                                        boxJson.getString("label")
                                    )
                                )
                            }
                        }

                        val processedBitmap = if (boundingBoxes.isNotEmpty()) {
                            drawBoundingBoxes(screenshotBitmap, boundingBoxes)
                        } else {
                            screenshotBitmap
                        }
                        
                        if (scanResult != null && !isMalicious) {
                            emit(ScanState.FinalResult(PhishResult(false, url, listOf("Verified safe by community scans"), "urlscan.io", processedBitmap)))
                        } else {
                            emit(ScanState.FinalResult(PhishResult(isMalicious, url, reasons, if (isMalicious) "Gemini AI" else "System Check", processedBitmap, confidenceScore = confidenceScore, boundingBoxes = boundingBoxes)))
                        }
                    }
                } catch (e: Exception) {
                     Log.e("PhishDetector", "Final AI analysis failed", e)
                     emit(ScanState.FinalResult(interimResult.copy(detectedBy = interimResult.detectedBy.replace(" (Interim)", ""), screenshot = screenshotBitmap)))
                }
            } else {
                 if (scanResult != null && !interimResult.isMalicious) {
                     emit(ScanState.FinalResult(PhishResult(false, url, listOf("Verified safe by community scans"), "urlscan.io", null)))
                 } else {
                     emit(ScanState.FinalResult(interimResult.copy(detectedBy = interimResult.detectedBy.replace(" (Interim)", ""))))
                 }
            }
        }
    }

    private fun drawBoundingBoxes(bitmap: Bitmap, boxes: List<BoundingBox>): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        
        val rectPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
            alpha = 180
        }

        val fillPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
            alpha = 40
        }

        val textPaint = Paint().apply {
            color = Color.RED
            textSize = 30f
            isFakeBoldText = true
        }

        val width = bitmap.width
        val height = bitmap.height

        for (box in boxes) {
            val left = box.xmin * width / 1000f
            val top = box.ymin * height / 1000f
            val right = box.xmax * width / 1000f
            val bottom = box.ymax * height / 1000f

            val rect = RectF(left, top, right, bottom)
            canvas.drawRect(rect, fillPaint)
            canvas.drawRect(rect, rectPaint)

            // Measure text to ensure it stays within bounds
            val textBounds = Rect()
            textPaint.getTextBounds(box.label, 0, box.label.length, textBounds)
            val textHeight = textBounds.height()
            
            // Draw label above box, or inside if too close to top
            val labelY = if (top - 15 < textHeight) {
                top + textHeight + 10 // Draw inside at the top
            } else {
                top - 10 // Draw above
            }
            
            // Optional: Draw a small background for the text for better visibility
            val textBgPaint = Paint().apply {
                color = Color.BLACK
                alpha = 140
            }
            canvas.drawRect(
                left,
                labelY - textHeight - 5,
                left + textPaint.measureText(box.label) + 10,
                labelY + 5,
                textBgPaint
            )

            canvas.drawText(box.label, left + 5, labelY, textPaint)
        }

        return mutableBitmap
    }

    private suspend fun downloadBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { BitmapFactory.decodeStream(it) }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PhishDetector", "Failed to download screenshot", e)
            null
        }
    }

    private fun checkUrlLocally(url: String): PhishResult {
        val lowerUrl = url.lowercase()
        val reasons = mutableListOf<String>()
        val maliciousKeywords = listOf(
            "paypa1", "paypaI", "paly-pal", "secure-bank", "banking-update", "verify-account",
            "account-locked", "unusual-activity", "suspicious-transaction", "billing-update",
            "credit-card-verify", "wallet-security", "chase-online", "wellsfargo-verify",
            "bankofamerica-secure", "hsbc-login", "citi-alert", "barclays-security",
            "netf1ix", "netflix-verify", "amzn-security", "amaz0n", "apple-id-login",
            "icloud-find", "microsoft-security", "outlook-verify", "office365-update",
            "google-account-verify", "g00gle", "faceb00k", "instagrann", "snapchat-verify",
            "urgent-action-required", "account-suspended", "limited-access", "login-update",
            "reset-password-now", "identity-verification", "security-alert-login",
            "immediate-attention", "terminate-account", "compliance-update",
            "blockchain-wallet", "crypto-security", "binance-verify", "coinbase-login",
            "metamask-update", "trustwallet-verify", "airdrop-claim", "seed-phrase-verify",
            "ledger-support", "trezor-security", "tax-refund-status", "gov-uk-verify",
            "irs-tax-update", "social-security-verify", "government-grant", "fine-payment-notice",
            "unclaimed-funds", "claim-your-prize", "gift-card-bonus", "reward-points-expiry",
            "iphone-giveaway", "survey-reward", "lottery-winner", "exclusive-offer-today"
        )

        maliciousKeywords.forEach { if (lowerUrl.contains(it)) reasons.add("Suspicious keyword detected: $it") }
        if (url.length > 100) reasons.add("Unusually long URL (often used to hide malicious domains)")
        if (url.contains("bit.ly") || url.contains("t.co") || url.contains("tinyurl.com") || url.contains("is.gd") || url.contains("buff.ly")) {
            reasons.add("URL shortener used (may obscure the final destination)")
        }

        val ipPattern = "^https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}".toRegex()
        if (ipPattern.containsMatchIn(lowerUrl)) {
            reasons.add("URL uses an IP address instead of a domain name")
        }

        if (lowerUrl.count { it == '.' } > 4) {
            reasons.add("Excessive number of subdomains detected")
        }

        return PhishResult(reasons.isNotEmpty(), url, reasons)
    }
}
