package com.example.polihackplm2.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BlocklistDao {
    @Query("SELECT * FROM blocked_domains ORDER BY blockedAt DESC")
    fun getAllBlocked(): Flow<List<BlockedDomain>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockDomain(domain: BlockedDomain)

    @Query("DELETE FROM blocked_domains WHERE domain = :domain")
    suspend fun deleteDomain(domain: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_domains WHERE domain = :domain LIMIT 1)")
    suspend fun isDomainBlocked(domain: String): Boolean

    @Query("SELECT COUNT(*) FROM blocked_domains")
    fun getBlockedCountFlow(): Flow<Int>

    @Query("DELETE FROM blocked_domains")
    suspend fun deleteAll()
}

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentScans(): Flow<List<ScanResultEntity>>

    @Insert
    suspend fun insertScan(scan: ScanResultEntity)

    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun getTotalScansCount(): Int

    @Query("SELECT COUNT(*) FROM scan_history WHERE title = 'Phishing Detected' OR title = 'Suspicious Link'")
    suspend fun getBlockedScansCount(): Int

    @Query("SELECT COUNT(*) FROM scan_history WHERE title = 'Suspicious Link'")
    suspend fun getWarningScansCount(): Int

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()
}
