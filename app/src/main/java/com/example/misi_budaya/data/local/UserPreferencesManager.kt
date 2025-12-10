package com.example.misi_budaya.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
        private val PREVIOUS_USER_UID = stringPreferencesKey("previous_user_uid")
        private val PREVIOUS_USER_EMAIL = stringPreferencesKey("previous_user_email")
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
     * Flow untuk observe previous user uid
     */
    val previousUserUidFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREVIOUS_USER_UID]
        }

    /**
     * Flow untuk observe previous user email
     */
    val previousUserEmailFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREVIOUS_USER_EMAIL]
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
     * Set previous user info (uid + email) ketika user online
     */
    suspend fun setPreviousUser(uid: String?, email: String?) {
        context.dataStore.edit { preferences ->
            if (uid != null) preferences[PREVIOUS_USER_UID] = uid else preferences.remove(PREVIOUS_USER_UID)
            if (email != null) preferences[PREVIOUS_USER_EMAIL] = email else preferences.remove(PREVIOUS_USER_EMAIL)
        }
    }

    /**
     * Clear stored previous user info (on logout)
     */
    suspend fun clearPreviousUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(PREVIOUS_USER_UID)
            preferences.remove(PREVIOUS_USER_EMAIL)
        }
    }

    // We rely on isOfflineModeFlow for observing offline status; no one-shot suspend helper needed.
}
