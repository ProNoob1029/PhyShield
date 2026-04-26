package com.example.polihackplm2.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polihackplm2.functionality.BlocklistManager
import com.example.polihackplm2.functionality.PhishDetector

@Composable
fun AlertScreen(
    scanState: PhishDetector.ScanState?,
    onBack: () -> Unit = {},
    onOpenUrl: () -> Unit = {},
    onScreenshotClick: (android.graphics.Bitmap) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier =
            Modifier
                .verticalScroll(scrollState)
                .navigationBarsPadding()
        ) {
            Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "PhishShield Analysis", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            if (scanState == null || scanState is PhishDetector.ScanState.Loading) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Starting deep analysis...", fontWeight = FontWeight.Medium)
                        Text("Checking local and community databases", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val result = when (scanState) {
                    is PhishDetector.ScanState.InterimAiResult -> scanState.result
                    is PhishDetector.ScanState.FinalResult -> scanState.result
                    else -> null
                }

                if (result != null) {
                    val isMalicious = result.isMalicious
                    val isFinal = scanState is PhishDetector.ScanState.FinalResult

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFinal) {
                                if (isMalicious) "Alert: phishing link detected" else "Analysis Complete"
                            } else {
                                "Initial AI Analysis"
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMalicious) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth().border(
                                1.dp,
                                if (isMalicious) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                RoundedCornerShape(12.dp)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isMalicious) "HIGH RISK — DO NOT OPEN" else "LIKELY SAFE",
                                    color = if (isMalicious) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = result.url,
                                    color = if (isMalicious) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        if (!isFinal) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Waiting for community scan and screenshot...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (isMalicious) "Why it was flagged" else "Analysis details",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (result.detectedBy.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Detection source: ${result.detectedBy}",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (result.screenshot != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Site Preview ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onScreenshotClick(result.screenshot) }
                            ) {
                                ZoomableImage(
                                    bitmap = result.screenshot,
                                    contentDescription = "Screenshot of the site",
                                    modifier = Modifier.aspectRatio(4f/3f),
                                    canZoomIn = false
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Robust splitting: Try our new ;;; first, then fallback to comma-fix for old data
                        val reasons = result.reasons.flatMap { 
                            if (it.contains(",The") || it.contains(",A ")) {
                                it.split(",(?=[A-Z])".toRegex())
                            } else {
                                listOf(it)
                            }
                        }

                        reasons.forEach { ReasonItem(it, color = if (isMalicious) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary); Spacer(modifier = Modifier.height(12.dp)) }

                        if (isFinal) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    if (isMalicious) {
                                        BlocklistManager.blockUrl(
                                            url = result.url,
                                            type = "MANUAL",
                                            reasons = result.reasons,
                                            detectedBy = result.detectedBy
                                        )
                                        Toast.makeText(context, "Site blocked and reported!", Toast.LENGTH_LONG).show()
                                        onBack()
                                    } else {
                                        onOpenUrl()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isMalicious) "Block and report" else "Open Link",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!isMalicious) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = onBack,
                                    modifier = Modifier.fillMaxWidth().height(56.dp).navigationBarsPadding(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Go Back")
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReasonItem(reason: String, color: Color = MaterialTheme.colorScheme.primary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = reason,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
