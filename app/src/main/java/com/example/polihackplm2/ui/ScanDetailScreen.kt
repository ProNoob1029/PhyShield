@file:Suppress("AssignedValueIsNeverRead")

package com.example.polihackplm2.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import com.example.polihackplm2.functionality.BlocklistManager
import com.example.polihackplm2.ui.theme.PhishBlue
import com.example.polihackplm2.ui.theme.PhishGreen
import com.example.polihackplm2.ui.theme.PhishOrange
import com.example.polihackplm2.ui.theme.PhishRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailScreen(
    threat: Threat, 
    onBack: () -> Unit, 
    onOpenUrl: (String) -> Unit,
    onDelete: () -> Unit,
    onScreenshotClick: (android.graphics.Bitmap) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val threatColor = when (threat.title) {
        "Phishing Detected" -> PhishRed
        "Suspicious Link" -> PhishOrange
        else -> PhishGreen
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Details", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PhishBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Website", fontSize = 14.sp, color = Color.Gray)
                    Text(text = threat.description, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "Classification", fontSize = 14.sp, color = Color.Gray)
                    Text(text = threat.title, color = threatColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "Scan Time", fontSize = 14.sp, color = Color.Gray)
                    Text(text = threat.time, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Analysis Reasons", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
            
            if (threat.reasons.isEmpty()) {
                Text(text = "No detailed reasons provided for this scan.", color = Color.Gray)
            } else {
                // Robust splitting: Try our new ;;; first, then fallback to comma-fix for old data
                val splitReasons = threat.reasons.flatMap { 
                    if (it.contains(",The") || it.contains(",A ")) {
                        it.split(",(?=[A-Z])".toRegex())
                    } else {
                        listOf(it)
                    }
                }
                splitReasons.forEach { reason ->
                    ReasonItem(reason, color = threatColor)
                }
            }

            if (threat.screenshotPath != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Site Preview ", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(threat.screenshotPath)
                            if (bitmap != null) {
                                onScreenshotClick(bitmap)
                            }
                        }
                ) {
                    ZoomableImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(threat.screenshotPath)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Site Screenshot",
                        modifier = Modifier.aspectRatio(4f/3f),
                        canZoomIn = false
                    )
                }
            }

            if (threat.title == "Safe Website") {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onOpenUrl(threat.description) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PhishBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open in Safe Preview", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                val context = LocalContext.current
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        BlocklistManager.blockUrl(
                            url = threat.description,
                            type = "MANUAL",
                            reasons = threat.reasons,
                            detectedBy = threat.detectedBy.ifEmpty { "User" }
                        )
                        Toast.makeText(context, "Site blocked and reported!", Toast.LENGTH_LONG).show()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PhishRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Block and report", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Activity") },
            text = { Text("Are you sure you want to permanently delete this scan record? This will update your dashboard stats.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Delete", color = PhishRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
