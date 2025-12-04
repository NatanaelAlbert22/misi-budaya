package com.example.misi_budaya.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore untuk simpan preferensi user terkait mode offline/online
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    
    companion object {
        private val IS_OFFLINE_MODE = booleanPreferencesKey("is_offline_mode")
        private val HAS_SEEN_ONLINE_PROMPT = booleanPreferencesKey("has_seen_online_prompt")
    }

    /**
     * Flow untuk observe status offline mode
     */
    val isOfflineModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_OFFLINE_MODE] ?: false
        }

    /**
     * Flow untuk cek apakah user sudah pernah lihat prompt switch ke online
     */
    val hasSeenOnlinePromptFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAS_SEEN_ONLINE_PROMPT] ?: false
        }

    /**
     * Set mode offline/online
     * @param isOffline true untuk offline mode, false untuk online mode
     */
    suspend fun setOfflineMode(isOffline: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_OFFLINE_MODE] = isOffline
        }
    }

    /**
     * Tandai bahwa user sudah pernah lihat prompt untuk switch ke online
     */
    suspend fun markOnlinePromptSeen() {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONLINE_PROMPT] = true
        }
    }

    /**
     * Reset flag prompt (misal untuk testing atau reset settings)
     */
    suspend fun resetOnlinePrompt() {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONLINE_PROMPT] = false
        }
    }

    /**
     * Get current offline mode status (suspend function untuk one-time read)
     */
    suspend fun isOfflineMode(): Boolean {
        var isOffline = false
        context.dataStore.data.map { preferences ->
            isOffline = preferences[IS_OFFLINE_MODE] ?: false
        }
        return isOffline
    }
}
