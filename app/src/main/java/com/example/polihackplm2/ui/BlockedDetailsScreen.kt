package com.example.polihackplm2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polihackplm2.db.BlockedDomain
import com.example.polihackplm2.functionality.BlocklistManager
import com.example.polihackplm2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedDetailsScreen(entity: BlockedDomain, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neutralized Threat", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = null,
                tint = PhishRed,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = entity.domain,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            val statusText = if (entity.blockType == "AUTOMATIC") "Automatically Neutralized" else "Manually Reported"
            Card(
                colors = CardDefaults.cardColors(containerColor = PhishRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = PhishRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Analysis Reasons",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
            )

            val rawReasons = if (entity.reasons.isNotEmpty()) entity.reasons.split(";;;") else emptyList()
            val reasons = rawReasons.flatMap { 
                if (it.contains(",The") || it.contains(",A ")) {
                    it.split(",(?=[A-Z])".toRegex())
                } else {
                    listOf(it)
                }
            }

            if (reasons.isEmpty()) {
                Text(text = "No detailed reasons stored.", color = Color.Gray, modifier = Modifier.align(Alignment.Start))
            } else {
                reasons.forEach { reason ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(PhishRed, RoundedCornerShape(3.dp)))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = reason, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = {
                    BlocklistManager.unblockUrl(entity.domain)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhishRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Unblock Domain", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
