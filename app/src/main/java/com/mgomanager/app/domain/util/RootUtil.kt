package com.mgomanager.app.domain.util

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootUtil @Inject constructor() {

    /**
     * Check if device has root access
     */
    suspend fun isRooted(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Request root access if not already granted
     */
    suspend fun requestRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            val shell = Shell.getShell()
            shell.isRoot
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Execute a single root command
     */
    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd(command).exec()
            if (result.isSuccess) {
                Result.success(result.out.joinToString("\n"))
            } else {
                Result.failure(Exception("Command failed: ${result.err.joinToString("\n")}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Execute multiple root commands
     */
    suspend fun executeCommands(commands: List<String>): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val result = Shell.cmd(*commands.toTypedArray()).exec()
                if (result.isSuccess) {
                    Result.success(result.out)
                } else {
                    Result.failure(Exception("Commands failed: ${result.err.joinToString("\n")}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Check if Monopoly Go is installed
     */
    suspend fun isMonopolyGoInstalled(): Boolean = withContext(Dispatchers.IO) {
        val result = executeCommand("pm list packages com.scopely.monopolygo")
        result.isSuccess && result.getOrNull()?.contains("com.scopely.monopolygo") == true
    }

    /**
     * Force stop Monopoly Go
     */
    suspend fun forceStopMonopolyGo(): Result<Unit> = withContext(Dispatchers.IO) {
        executeCommand("am force-stop com.scopely.monopolygo").map { }
    }

    /**
     * Launch Monopoly Go
     */
    suspend fun launchMonopolyGo(): Result<Unit> = withContext(Dispatchers.IO) {
        executeCommand("monkey -p com.scopely.monopolygo -c android.intent.category.LAUNCHER 1").map { }
    }
}
