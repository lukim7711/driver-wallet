package com.driverwallet.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "driver_wallet_prefs")

@Singleton
class AppDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[HAS_SEEN_ONBOARDING] ?: false }

    suspend fun setOnboardingSeen() {
        context.dataStore.edit { prefs ->
            prefs[HAS_SEEN_ONBOARDING] = true
        }
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[DARK_MODE] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }
}
