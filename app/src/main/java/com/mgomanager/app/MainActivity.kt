package com.mgomanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.mgomanager.app.domain.util.PermissionManager
import com.mgomanager.app.domain.util.RootUtil
import com.mgomanager.app.ui.navigation.AppNavGraph
import com.mgomanager.app.ui.theme.MGOManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootUtil: RootUtil

    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MGOManagerTheme {
                val navController = rememberNavController()

                var isReady by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    // Check prerequisites
                    val isRooted = rootUtil.requestRootAccess()
                    val hasPermissions = permissionManager.hasStoragePermissions()
                    val isMGOInstalled = rootUtil.isMonopolyGoInstalled()

                    when {
                        !isRooted -> errorMessage = "Root-Zugriff erforderlich"
                        !hasPermissions -> errorMessage = "Speicher-Berechtigungen erforderlich"
                        !isMGOInstalled -> errorMessage = "Monopoly Go nicht installiert"
                        else -> isReady = true
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        errorMessage != null -> {
                            // Show error dialog
                            AlertDialog(
                                onDismissRequest = { finish() },
                                title = { Text("Fehler") },
                                text = { Text(errorMessage!!) },
                                confirmButton = {
                                    TextButton(onClick = { finish() }) {
                                        Text("Schließen")
                                    }
                                }
                            )
                        }
                        isReady -> AppNavGraph(navController)
                        else -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
