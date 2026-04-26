package com.example.polihackplm2.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.polihackplm2.db.BlockedDomain
import com.example.polihackplm2.functionality.BlocklistManager
import com.example.polihackplm2.functionality.HomeRefreshManager
import com.example.polihackplm2.functionality.PhishDetector
import com.example.polihackplm2.functionality.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PhishShieldViewModel : ViewModel() {
    private val _currentUrl = MutableStateFlow("")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _scanState = MutableStateFlow<PhishDetector.ScanState?>(null)
    val scanState: StateFlow<PhishDetector.ScanState?> = _scanState.asStateFlow()

    private val _selectedBlockedDomain = MutableStateFlow("")

    private val _selectedEntity = MutableStateFlow<BlockedDomain?>(null)
    val selectedEntity = _selectedEntity.asStateFlow()

    private val _currentThreat = MutableStateFlow<Threat?>(null)
    val currentThreat: StateFlow<Threat?> = _currentThreat.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _pendingUrl = MutableStateFlow<String?>(null)
    val pendingUrl: StateFlow<String?> = _pendingUrl.asStateFlow()

    fun setCurrentUrl(url: String) {
        _currentUrl.value = url
    }

    fun setSelectedBlockedDomain(domain: String) {
        _selectedBlockedDomain.value = domain
    }

    fun setSelectedEntity(entity: BlockedDomain) {
        _selectedEntity.value = entity
    }

    fun setCurrentThreat(threat: Threat) {
        _currentThreat.value = threat
    }

    fun setPendingUrl(url: String?) {
        _pendingUrl.value = url
    }

    fun consumePendingUrl() {
        _pendingUrl.value = null
    }

    fun startScan(url: String, context: Context) {
        _currentUrl.value = url
        _scanState.value = PhishDetector.ScanState.Loading
        viewModelScope.launch {
            val autoBlockEnabled = SettingsManager.getAutoBlock(context).first()
            PhishDetector.checkUrlFlow(context, url).collect { state ->
                _scanState.value = state
                if (state is PhishDetector.ScanState.FinalResult) {
                    val result = state.result
                    val screenshotPath = result.screenshot?.let { saveScreenshot(it, context) }
                    HomeRefreshManager.addScanResult(
                        result.isMalicious,
                        result.url,
                        result.reasons,
                        result.detectedBy,
                        screenshotPath,
                        result.boundingBoxes
                    )

                    if (autoBlockEnabled && result.isMalicious && result.confidenceScore >= 70) {
                        BlocklistManager.blockUrl(
                            url = result.url,
                            type = "AUTOMATIC",
                            reasons = result.reasons,
                            detectedBy = result.detectedBy
                        )
                        _events.emit("Threat neutralized automatically (70%+ confidence)")
                    }
                }
            }
        }
    }

    fun showPreviousResult(threat: Threat) {
        _currentUrl.value = threat.description
        val bitmap = threat.screenshotPath?.let { loadScreenshot(it) }
        val result = PhishDetector.PhishResult(
            isMalicious = threat.isMalicious,
            url = threat.description,
            reasons = threat.reasons,
            detectedBy = threat.detectedBy,
            screenshot = bitmap,
            boundingBoxes = threat.boundingBoxes
        )
        _scanState.value = PhishDetector.ScanState.FinalResult(result)
    }

    private suspend fun saveScreenshot(bitmap: Bitmap, context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val filename = "screenshot_${UUID.randomUUID()}.png"
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("PhishShieldViewModel", "Failed to save screenshot", e)
            null
        }
    }

    private fun loadScreenshot(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            Log.e("PhishShieldViewModel", "Failed to load screenshot", e)
            null
        }
    }
}
