package com.driverwallet.app.core.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.feature.settings.domain.SettingsKeys
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _showOnboarding = MutableStateFlow<Boolean?>(null)
    val showOnboarding: StateFlow<Boolean?> = _showOnboarding.asStateFlow()

    init {
        checkOnboarding()
    }

    private fun checkOnboarding() {
        viewModelScope.launch {
            val seen = settingsRepository.getSetting(SettingsKeys.HAS_SEEN_ONBOARDING)
            _showOnboarding.value = seen != "true"
        }
    }

    fun markOnboardingComplete() {
        viewModelScope.launch {
            settingsRepository.saveSetting(SettingsKeys.HAS_SEEN_ONBOARDING, "true")
            _showOnboarding.value = false
        }
    }
}
