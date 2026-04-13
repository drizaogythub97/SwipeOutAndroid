package com.swipeout.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store = context.dataStore

    companion object {
        val KEY_ONBOARDING   = booleanPreferencesKey("has_seen_onboarding")
        val KEY_BYTES_FREED  = longPreferencesKey("total_bytes_freed")
        val KEY_LANGUAGE     = stringPreferencesKey("app_language") // "auto" | "pt" | "en"
    }

    val hasSeenOnboarding: Flow<Boolean> = store.data.map { it[KEY_ONBOARDING] ?: false }
    val totalBytesFreed: Flow<Long>      = store.data.map { it[KEY_BYTES_FREED] ?: 0L }
    val appLanguage: Flow<String>        = store.data.map { it[KEY_LANGUAGE] ?: "auto" }

    suspend fun setOnboardingComplete() {
        store.edit { it[KEY_ONBOARDING] = true }
    }

    suspend fun addBytesFreed(bytes: Long) {
        store.edit { prefs ->
            prefs[KEY_BYTES_FREED] = (prefs[KEY_BYTES_FREED] ?: 0L) + bytes
        }
    }

    suspend fun setLanguage(lang: String) {
        store.edit { it[KEY_LANGUAGE] = lang }
    }
}
