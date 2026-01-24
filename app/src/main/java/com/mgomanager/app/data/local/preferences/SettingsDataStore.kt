package com.mgomanager.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore wrapper for app settings
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val ACCOUNT_PREFIX = stringPreferencesKey("account_prefix")
        private val BACKUP_ROOT_PATH = stringPreferencesKey("backup_root_path")
        private val CURRENT_SESSION_ID = stringPreferencesKey("current_session_id")
        private val APP_START_COUNT = intPreferencesKey("app_start_count")

        const val DEFAULT_PREFIX = "MGO_"
        const val DEFAULT_BACKUP_PATH = "/storage/emulated/0/mgo/backups/"
    }

    /**
     * Get account prefix
     */
    val accountPrefix: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ACCOUNT_PREFIX] ?: DEFAULT_PREFIX
    }

    /**
     * Set account prefix
     */
    suspend fun setAccountPrefix(prefix: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCOUNT_PREFIX] = prefix
        }
    }

    /**
     * Get backup root path
     */
    val backupRootPath: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BACKUP_ROOT_PATH] ?: DEFAULT_BACKUP_PATH
    }

    /**
     * Set backup root path
     */
    suspend fun setBackupRootPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[BACKUP_ROOT_PATH] = path
        }
    }

    /**
     * Get current session ID
     */
    val currentSessionId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_SESSION_ID] ?: generateNewSessionId()
    }

    /**
     * Generate and save new session ID
     */
    suspend fun generateNewSession(): String {
        val newSessionId = UUID.randomUUID().toString()
        context.dataStore.edit { preferences ->
            preferences[CURRENT_SESSION_ID] = newSessionId
        }
        return newSessionId
    }

    /**
     * Get app start count
     */
    val appStartCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[APP_START_COUNT] ?: 0
    }

    /**
     * Increment app start count
     */
    suspend fun incrementAppStartCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[APP_START_COUNT] ?: 0
            preferences[APP_START_COUNT] = currentCount + 1
        }
    }

    private fun generateNewSessionId(): String = UUID.randomUUID().toString()
}
