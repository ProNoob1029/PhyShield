package com.example.polihackplm2.ui

import com.example.polihackplm2.functionality.PhishDetector

data class Threat(
    val id: Int = 0,
    val title: String,
    val description: String,
    val time: String,
    val screenshotPath: String? = null,
    val isMalicious: Boolean = false,
    val reasons: List<String> = emptyList(),
    val detectedBy: String = "",
    val boundingBoxes: List<PhishDetector.BoundingBox> = emptyList()
)
