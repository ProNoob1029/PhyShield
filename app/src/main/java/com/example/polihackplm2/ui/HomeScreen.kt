package com.example.polihackplm2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polihackplm2.functionality.HomeData
import com.example.polihackplm2.functionality.HomeRefreshManager

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onThreatClick: (Threat) -> Unit = {}
) {
    val homeData by HomeRefreshManager.homeDataFlow.collectAsState(
        initial = HomeData(0, 0, 100, emptyList())
    )

    // Using a parent Box to ensure blue background is drawn behind status bar
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Immersive Header Surface
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Text(text = "Protection active", color = MaterialTheme.colorScheme.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Monitoring links & QR codes", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatCard(homeData.blockedToday.toString(), "blocked today")
                        StatCard(homeData.warnings.toString(), "warnings")
                        StatCard("${homeData.safeScore}%", "safe score")
                    }
                }
            }

            // Recent Activity Content below the header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(text = "Recent activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.onBackground)
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    homeData.recentScans.forEach { scan ->
                        ThreatItem(scan, onClick = { onThreatClick(scan) })
                    }
                }
            }
        }
    }
}

@Composable
fun ThreatItem(threat: Threat, onClick: () -> Unit) {
    val threatColor = when (threat.title) {
        "Phishing Detected" -> MaterialTheme.colorScheme.error
        "Suspicious Link" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(4.dp, 40.dp).clip(RoundedCornerShape(2.dp)).background(threatColor))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = threat.title, color = threatColor, fontWeight = FontWeight.Bold)
                Text(text = threat.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}
