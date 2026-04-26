package com.example.polihackplm2.network.urlscan

import com.example.polihackplm2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

interface UrlScanService {
    /**
     * Submits a URL for scanning.
     * @param url The URL to scan.
     * @param visibility The visibility of the scan ("public", "unlisted", "private"). Default is "public".
     * @param customAgent Optional User-Agent string to use for the scan.
     * @return [ScanSubmissionResult] containing the UUID of the scan.
     */
    suspend fun submitUrl(url: String, visibility: String = "public", customAgent: String? = null): ScanSubmissionResult

    /**
     * Retrieves the scan result for a given UUID.
     * @param uuid The UUID of the scan.
     * @return [ScanResult] if the scan is finished, or null if it's still pending or not found.
     */
    suspend fun getScanResult(uuid: String): ScanResult?

    /**
     * Polls the API until the scan is finished or the timeout is reached.
     * @param uuid The UUID of the scan.
     * @param timeoutMillis Maximum time to wait in milliseconds. Default is 60 seconds.
     * @param pollIntervalMillis Time between polls in milliseconds. Default is 5 seconds.
     * @return [ScanResult] if successful, or null if it timed out.
     */
    suspend fun waitForScanResult(
        uuid: String,
        timeoutMillis: Long = 60_000,
        pollIntervalMillis: Long = 5_000
    ): ScanResult?
}

class UrlScanServiceImpl(
    private val client: OkHttpClient = OkHttpClient()
) : UrlScanService {

    companion object {
        private const val BASE_URL = "https://urlscan.io/api/v1"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    override suspend fun submitUrl(url: String, visibility: String, customAgent: String?): ScanSubmissionResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.URLSCAN_API_KEY
        val jsonRequest = JSONObject().apply {
            put("url", url)
            put("visibility", visibility)
            customAgent?.let { put("customagent", it) }
        }

        val request = Request.Builder()
            .url("$BASE_URL/scan/")
            .addHeader("API-Key", apiKey)
            .post(jsonRequest.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful || responseBody == null) {
                throw UrlScanException("Failed to submit URL. Code: ${response.code}, Body: $responseBody")
            }

            try {
                val json = JSONObject(responseBody)
                return@withContext ScanSubmissionResult(
                    message = json.optString("message", ""),
                    uuid = json.getString("uuid"),
                    resultUrl = json.getString("result"),
                    apiUrl = json.getString("api"),
                    visibility = json.getString("visibility")
                )
            } catch (e: Exception) {
                throw UrlScanException("Failed to parse submission response", e)
            }
        }
    }

    override suspend fun getScanResult(uuid: String): ScanResult? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/result/$uuid/")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 404) {
                // Scan not finished yet
                return@withContext null
            }
            
            val responseBody = response.body?.string()
            if (!response.isSuccessful || responseBody == null) {
                throw UrlScanException("Failed to get scan result. Code: ${response.code}, Body: $responseBody")
            }

            try {
                val json = JSONObject(responseBody)
                
                val page = json.getJSONObject("page")
                val pageUrl = page.getString("url")
                
                val verdicts = json.getJSONObject("verdicts")
                val overall = verdicts.getJSONObject("overall")
                val malicious = overall.getBoolean("malicious")
                val score = overall.optInt("score", 0)
                
                val tagsArray = overall.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsArray != null) {
                    for (i in 0 until tagsArray.length()) {
                        tags.add(tagsArray.getString(i))
                    }
                }

                return@withContext ScanResult(
                    uuid = uuid,
                    pageUrl = pageUrl,
                    screenshotUrl = "https://urlscan.io/screenshots/$uuid.png",
                    reportUrl = "https://urlscan.io/result/$uuid/",
                    malicious = malicious,
                    score = score,
                    tags = tags
                )
            } catch (e: Exception) {
                throw UrlScanException("Failed to parse scan result", e)
            }
        }
    }

    override suspend fun waitForScanResult(
        uuid: String,
        timeoutMillis: Long,
        pollIntervalMillis: Long
    ): ScanResult? {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            val result = getScanResult(uuid)
            if (result != null) {
                return result
            }
            delay(pollIntervalMillis)
        }
        return null
    }
}
