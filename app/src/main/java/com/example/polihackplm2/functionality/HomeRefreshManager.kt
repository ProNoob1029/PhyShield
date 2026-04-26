package com.example.polihackplm2.functionality

import androidx.compose.ui.graphics.toArgb
import com.example.polihackplm2.db.BlocklistDao
import com.example.polihackplm2.db.ScanHistoryDao
import com.example.polihackplm2.db.ScanResultEntity
import com.example.polihackplm2.ui.Threat
import com.example.polihackplm2.ui.theme.PhishGreen
import com.example.polihackplm2.ui.theme.PhishOrange
import com.example.polihackplm2.ui.theme.PhishRed
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class HomeData(
    val blockedToday: Int,
    val warnings: Int,
    val safeScore: Int,
    val recentScans: List<Threat>
)

object HomeRefreshManager {
    private var dao: ScanHistoryDao? = null
    private var blocklistDao: BlocklistDao? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _refreshTrigger = MutableStateFlow(0)

    fun init(scanHistoryDao: ScanHistoryDao, blocklistDao: BlocklistDao) {
        this.dao = scanHistoryDao
        this.blocklistDao = blocklistDao
    }

    fun clearAll(onComplete: () -> Unit = {}) {
        scope.launch {
            dao?.deleteAll()
            _refreshTrigger.value += 1
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    fun deleteScanResult(id: Int, onComplete: () -> Unit = {}) {
        scope.launch {
            dao?.deleteById(id)
            _refreshTrigger.value += 1
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val homeDataFlow = _refreshTrigger.flatMapLatest {
        val historyFlow = dao?.getRecentScans() ?: flowOf(emptyList())
        val blockedCountFlow = blocklistDao?.getBlockedCountFlow() ?: flowOf(0)
        
        combine(historyFlow, blockedCountFlow) { entities, blockedCount ->
            val total = dao?.getTotalScansCount() ?: 0
            val warnings = dao?.getWarningScansCount() ?: 0
            
            val safeScans = total - (dao?.getBlockedScansCount() ?: 0)
            val score = if (total > 0) {
                ((safeScans.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else {
                100
            }

            HomeData(
                blockedToday = blockedCount, // USE LIVE DB COUNT
                warnings = warnings,
                safeScore = score,
                recentScans = entities.map { it.toThreat() }
            )
        }
    }.flowOn(Dispatchers.IO) // ENSURE DB CALLS DON'T BLOCK MAIN THREAD

    fun addScanResult(
        isMalicious: Boolean,
        url: String,
        reasons: List<String>,
        detectedBy: String,
        screenshotPath: String? = null,
        boundingBoxes: List<PhishDetector.BoundingBox> = emptyList()
    ) {
        scope.launch {
            val title = if (isMalicious) {
                if (reasons.any { it.contains("keyword", ignoreCase = true) || it.contains("Typosquatting", ignoreCase = true) }) 
                    "Phishing Detected" 
                else "Suspicious Link"
            } else {
                "Safe Website"
            }

            val color = when (title) {
                "Phishing Detected" -> PhishRed
                "Suspicious Link" -> PhishOrange
                else -> PhishGreen
            }

            val boxesJson = if (boundingBoxes.isNotEmpty()) {
                val array = JSONArray()
                for (box in boundingBoxes) {
                    val obj = JSONObject().apply {
                        put("ymin", box.ymin)
                        put("xmin", box.xmin)
                        put("ymax", box.ymax)
                        put("xmax", box.xmax)
                        put("label", box.label)
                    }
                    array.put(obj)
                }
                array.toString()
            } else null

            val entity = ScanResultEntity(
                url = url,
                title = title,
                color = color.toArgb(),
                reasons = reasons.joinToString(";;;"),
                detectedBy = detectedBy,
                screenshotPath = screenshotPath,
                boundingBoxesJson = boxesJson
            )
            
            dao?.insertScan(entity)
            _refreshTrigger.value += 1 // Force UI refresh
        }
    }

    private fun ScanResultEntity.toThreat(): Threat {
        val isMalicious = this.title == "Phishing Detected" || this.title == "Suspicious Link"
        
        val boxes = mutableListOf<PhishDetector.BoundingBox>()
        this.boundingBoxesJson?.let { jsonStr ->
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    boxes.add(
                        PhishDetector.BoundingBox(
                            obj.getInt("ymin"),
                            obj.getInt("xmin"),
                            obj.getInt("ymax"),
                            obj.getInt("xmax"),
                            obj.getString("label")
                        )
                    )
                }
            } catch (_: Exception) {
                // Ignore parsing errors
            }
        }

        return Threat(
            id = this.id,
            title = this.title,
            description = this.url,
            time = formatTime(this.timestamp),
            screenshotPath = this.screenshotPath,
            isMalicious = isMalicious,
            reasons = this.reasons.split(";;;").filter { it.isNotBlank() },
            detectedBy = this.detectedBy,
            boundingBoxes = boxes
        )
    }

    private fun formatTime(timestamp: Long): String {
        val seconds = (System.currentTimeMillis() - timestamp) / 1000
        return when {
            seconds < 60 -> "Just now"
            seconds < 3600 -> "${seconds / 60} min ago"
            else -> "${seconds / 3600} hr ago"
        }
    }
}
