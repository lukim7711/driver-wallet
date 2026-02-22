package com.driverwallet.app.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.feature.settings.domain.SettingsKeys
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = settingsRepository
        .observeSetting(SettingsKeys.DARK_MODE)
        .map { it == "true" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )
}
