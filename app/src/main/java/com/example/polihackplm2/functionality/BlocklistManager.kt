package com.example.polihackplm2.functionality

import android.util.Log
import com.example.polihackplm2.db.BlockedDomain
import com.example.polihackplm2.db.BlocklistDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

object BlocklistManager {
    private var dao: BlocklistDao? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _blockedDomains = MutableStateFlow<List<BlockedDomain>>(emptyList())
    val blockedDomainsFlow = _blockedDomains.asStateFlow()

    fun init(blocklistDao: BlocklistDao) {
        dao = blocklistDao
        // Observe changes in database and update flow
        scope.launch {
            dao?.getAllBlocked()?.collect { entities ->
                _blockedDomains.value = entities
            }
        }
    }

    fun blockUrl(url: String, type: String = "MANUAL", reasons: List<String> = emptyList(), detectedBy: String = "User") {
        val domain = getDomain(url)
        scope.launch {
            dao?.blockDomain(
                BlockedDomain(
                    domain = domain,
                    blockType = type,
                    reasons = reasons.joinToString(";;;"),
                    detectedBy = detectedBy
                )
            )
        }
        Log.d("BlocklistManager", "Blocked domain: $domain ($type)")
    }

    fun unblockUrl(domain: String) {
        scope.launch {
            dao?.deleteDomain(domain)
        }
        Log.d("BlocklistManager", "Unblocked domain: $domain")
    }

    fun clearAll(onComplete: () -> Unit = {}) {
        scope.launch {
            dao?.deleteAll()
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    suspend fun isBlocked(url: String): Boolean {
        val domain = getDomain(url)
        return dao?.isDomainBlocked(domain) ?: false
    }

    private fun getDomain(url: String): String {
        return try {
            val uri = url.toUri()
            uri.host?.lowercase()?.removePrefix("www.") ?: url.lowercase()
        } catch (_: Exception) {
            url.lowercase()
        }
    }
}
