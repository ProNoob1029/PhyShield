package com.example.polihackplm2.network.urlscan

/**
 * Result of submitting a URL for scanning.
 */
data class ScanSubmissionResult(
    val message: String,
    val uuid: String,
    val resultUrl: String,
    val apiUrl: String,
    val visibility: String
)

/**
 * Detailed result of a completed scan.
 */
data class ScanResult(
    val uuid: String,
    val pageUrl: String,
    val screenshotUrl: String,
    val reportUrl: String,
    val malicious: Boolean,
    val score: Int,
    val tags: List<String>
)

/**
 * Exception thrown when a urlscan.io API request fails.
 */
class UrlScanException(message: String, cause: Throwable? = null) : Exception(message, cause)
