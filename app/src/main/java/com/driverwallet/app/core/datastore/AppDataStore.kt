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
    private object Keys {
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val hasSeenOnboarding: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.HAS_SEEN_ONBOARDING] ?: false }

    suspend fun setHasSeenOnboarding(value: Boolean) {
        context.dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = value }
    }

    val darkMode: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.DARK_MODE] ?: false }

    suspend fun setDarkMode(value: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = value }
    }
}
