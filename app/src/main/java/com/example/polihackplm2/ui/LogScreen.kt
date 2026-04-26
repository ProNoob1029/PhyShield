package com.example.polihackplm2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polihackplm2.db.BlockedDomain
import com.example.polihackplm2.functionality.BlocklistManager

@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    onBlockedItemClick: (BlockedDomain) -> Unit = {}
) {
    val blockedDomains by BlocklistManager.blockedDomainsFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        PhishShieldHeader("Activity Log")
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Text(
                text = "User Blocklist",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (blockedDomains.isEmpty()) {
                Text(text = "No websites blocked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    blockedDomains.forEach { domainEntity ->
                        BlockedItem(domainEntity, onClick = { onBlockedItemClick(domainEntity) })
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedItem(entity: BlockedDomain, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = entity.domain, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                val statusText = if (entity.blockType == "AUTOMATIC") "Automatically Neutralized" else "Manually blocked and reported"
                Text(text = statusText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}