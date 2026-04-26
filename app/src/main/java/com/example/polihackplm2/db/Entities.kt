package com.example.polihackplm2.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_domains")
data class BlockedDomain(
    @PrimaryKey val domain: String,
    val blockedAt: Long = System.currentTimeMillis(),
    val blockType: String, // "MANUAL" or "AUTOMATIC"
    val reasons: String,
    val detectedBy: String
)

@Entity(tableName = "scan_history")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val color: Int, // Store as ARGB color Int
    val reasons: String, // Store as comma-separated string
    val detectedBy: String,
    val timestamp: Long = System.currentTimeMillis(),
    val screenshotPath: String? = null,
    val boundingBoxesJson: String? = null
)
