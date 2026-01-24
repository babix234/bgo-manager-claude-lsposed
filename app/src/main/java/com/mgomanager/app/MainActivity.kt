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
                var showPermissionDialog by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    // Check prerequisites
                    val isRooted = rootUtil.requestRootAccess()
                    val hasPermissions = permissionManager.hasStoragePermissions()
                    val isMGOInstalled = rootUtil.isMonopolyGoInstalled()

                    when {
                        !isRooted -> errorMessage = "Root-Zugriff erforderlich"
                        !hasPermissions -> showPermissionDialog = true
                        !isMGOInstalled -> errorMessage = "Monopoly Go nicht installiert"
                        else -> isReady = true
                    }
                }

                // Recheck permissions when resuming from settings
                DisposableEffect(Unit) {
                    val listener = object : android.app.Application.ActivityLifecycleCallbacks {
                        override fun onActivityResumed(activity: android.app.Activity) {
                            if (activity == this@MainActivity && showPermissionDialog) {
                                if (permissionManager.hasStoragePermissions()) {
                                    showPermissionDialog = false
                                    if (errorMessage == null) {
                                        isReady = true
                                    }
                                }
                            }
                        }
                        override fun onActivityPaused(activity: android.app.Activity) {}
                        override fun onActivityStarted(activity: android.app.Activity) {}
                        override fun onActivityStopped(activity: android.app.Activity) {}
                        override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: Bundle?) {}
                        override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: Bundle) {}
                        override fun onActivityDestroyed(activity: android.app.Activity) {}
                    }
                    application.registerActivityLifecycleCallbacks(listener)
                    onDispose {
                        application.unregisterActivityLifecycleCallbacks(listener)
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
                        showPermissionDialog -> {
                            // Show permission request dialog
                            AlertDialog(
                                onDismissRequest = { },
                                title = { Text("Berechtigungen erforderlich") },
                                text = {
                                    Text("Diese App benötigt Zugriff auf den Speicher, um Backups zu erstellen. Bitte erteile die erforderlichen Berechtigungen in den Einstellungen.")
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        permissionManager.requestStoragePermissions(this@MainActivity)
                                    }) {
                                        Text("Berechtigungen erteilen")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { finish() }) {
                                        Text("Abbrechen")
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
