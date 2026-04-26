package com.example.polihackplm2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polihackplm2.functionality.BlocklistManager
import com.example.polihackplm2.functionality.HomeRefreshManager
import com.example.polihackplm2.functionality.SettingsManager
import com.example.polihackplm2.ui.theme.PhishRed
import kotlinx.coroutines.launch

@Suppress("AssignedValueIsNeverRead")
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val sensitivity by SettingsManager.getSensitivity(context).collectAsState(initial = 1)
    val autoBlock by SettingsManager.getAutoBlock(context).collectAsState(initial = false)
    val liveRefresh by SettingsManager.getLiveRefresh(context).collectAsState(initial = true)
    val jsEnabled by SettingsManager.getJavaScriptEnabled(context).collectAsState(initial = false)

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearBlocklistDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        PhishShieldHeader("Settings")
        
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            // --- PROTECTION SECTION ---
            Text("Security", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            
            SettingToggle(
                title = "Automatic Protection",
                description = "Automatically block 100% verified phishing links",
                icon = Icons.Default.Shield,
                checked = autoBlock,
                onCheckedChange = { scope.launch { SettingsManager.setAutoBlock(context, it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingToggle(
                title = "JavaScript in Preview",
                description = "Enable JavaScript when viewing sites in Safe Preview",
                icon = Icons.Default.Code,
                checked = jsEnabled,
                onCheckedChange = { scope.launch { SettingsManager.setJavaScriptEnabled(context, it) } }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("AI Sensitivity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = when(sensitivity) {
                    0 -> "Cautious: Fewer flags, higher certainty required."
                    1 -> "Balanced: Optimal protection for daily use."
                    else -> "Aggressive: Maximum protection, may flag suspicious unknowns."
                },
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = sensitivity.toFloat(),
                onValueChange = { scope.launch { SettingsManager.setSensitivity(context, it.toInt()) } },
                valueRange = 0f..2f,
                steps = 1
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // --- APP BEHAVIOR ---
            Text("Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            
            SettingToggle(
                title = "Real-Time Refresh",
                description = "Update dashboard stats while app is open",
                icon = Icons.Default.Refresh,
                checked = liveRefresh,
                onCheckedChange = { scope.launch { SettingsManager.setLiveRefresh(context, it) } }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // --- DANGER ZONE ---
            Text("Danger Zone", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PhishRed, modifier = Modifier.padding(bottom = 16.dp))
            
            DangerButton(
                text = "Clear Scan History",
                onClick = { showClearHistoryDialog = true }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DangerButton(
                text = "Reset User Blocklist",
                onClick = { showClearBlocklistDialog = true }
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            // About info
            Text(
                text = "PhishShield v1.0.4",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Confirmation Dialogs
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            confirmButton = { 
                TextButton(onClick = { 
                    HomeRefreshManager.clearAll { 
                        showClearHistoryDialog = false 
                    }
                }) { Text("Clear All", color = PhishRed) } 
            },
            dismissButton = { TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel") } },
            title = { Text("Clear History?") },
            text = { Text("This will permanently delete all records of sites you've scanned. This action cannot be undone.") }
        )
    }

    if (showClearBlocklistDialog) {
        AlertDialog(
            onDismissRequest = { showClearBlocklistDialog = false },
            confirmButton = { 
                TextButton(onClick = { 
                    BlocklistManager.clearAll { 
                        showClearBlocklistDialog = false 
                    }
                }) { Text("Reset", color = PhishRed) } 
            },
            dismissButton = { TextButton(onClick = { showClearBlocklistDialog = false }) { Text("Cancel") } },
            title = { Text("Reset Blocklist?") },
            text = { Text("This will unblock every site you have manually reported. You will be warned about them again if scanned.") }
        )
    }
}

@Composable
fun SettingToggle(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PhishRed),
        border = androidx.compose.foundation.BorderStroke(1.dp, PhishRed),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}
