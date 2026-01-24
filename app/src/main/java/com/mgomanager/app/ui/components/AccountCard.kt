package com.mgomanager.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgomanager.app.data.model.Account

@Composable
fun AccountCard(
    account: Account,
    onCardClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onCardClick,
        modifier = modifier,
        border = BorderStroke(3.dp, account.getBorderColor()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Account name
            Text(
                text = account.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // User ID
            Text(
                text = "ID: ${account.shortUserId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Timestamps
            Text(
                text = "⏱ ${account.getFormattedLastPlayedAt()}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "💾 ${account.getFormattedCreatedAt()}",
                style = MaterialTheme.typography.bodySmall
            )

            // Error message if present
            if (account.hasError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ERROR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Restore button
            Button(
                onClick = onRestoreClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("RESTORE")
            }
        }
    }
}
