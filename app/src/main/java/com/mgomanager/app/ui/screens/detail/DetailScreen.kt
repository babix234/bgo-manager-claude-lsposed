package com.mgomanager.app.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    accountId: Long,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.account?.fullName ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        uiState.account?.let { account ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Allgemeine Informationen
                Text("Allgemeine Informationen", style = MaterialTheme.typography.titleMedium)
                DetailInfoItem("Name", account.fullName)
                DetailInfoItem("Erstellt am", account.getFormattedCreatedAt())
                DetailInfoItem("Zuletzt gespielt", account.getFormattedLastPlayedAt())

                Divider()

                // Section: IDs
                Text("IDs", style = MaterialTheme.typography.titleMedium)
                DetailInfoItem("User ID", account.userId)
                DetailInfoItem("GAID", account.gaid)
                DetailInfoItem("Device Token", account.deviceToken)
                DetailInfoItem("App Set ID", account.appSetId)
                DetailInfoItem("SSAID", account.ssaid)

                Divider()

                // Section: Status
                Text("Status", style = MaterialTheme.typography.titleMedium)
                DetailInfoItem("Sus Level", account.susLevel.displayName)
                DetailInfoItem("Error", if (account.hasError) "Ja" else "Nein")

                if (account.hasFacebookLink) {
                    Divider()
                    Text("Facebook Verbindung", style = MaterialTheme.typography.titleMedium)
                    DetailInfoItem("Username", account.fbUsername ?: "")
                    DetailInfoItem("Passwort", account.fbPassword ?: "")
                    DetailInfoItem("2FA", account.fb2FA ?: "")
                    DetailInfoItem("Temp-Mail", account.fbTempMail ?: "")
                }

                Divider()

                // Section: Dateisystem
                Text("Dateisystem", style = MaterialTheme.typography.titleMedium)
                DetailInfoItem("Backup-Pfad", account.backupPath)
                DetailInfoItem("Eigentümer", "${account.fileOwner}:${account.fileGroup}")
                DetailInfoItem("Berechtigungen", account.filePermissions)

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.showRestoreDialog() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RESTORE")
                    }
                    OutlinedButton(
                        onClick = { viewModel.showEditDialog() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BEARBEITEN")
                    }
                    OutlinedButton(
                        onClick = { viewModel.showDeleteDialog() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("LÖSCHEN")
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideRestoreDialog() },
            title = { Text("Wiederherstellung") },
            text = { Text("Account '${uiState.account?.fullName}' wiederherstellen?") },
            confirmButton = {
                TextButton(onClick = { viewModel.restoreAccount() }) {
                    Text("Ja")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideRestoreDialog() }) {
                    Text("Nein")
                }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Löschen") },
            text = { Text("Account '${uiState.account?.fullName}' wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount { navController.popBackStack() }
                }) {
                    Text("Ja")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Nein")
                }
            }
        )
    }
}

@Composable
fun DetailInfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
