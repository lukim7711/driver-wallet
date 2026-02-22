package com.driverwallet.app.core.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.driverwallet.app.feature.dashboard.ui.DashboardScreen
import com.driverwallet.app.feature.debt.ui.form.DebtFormScreen
import com.driverwallet.app.feature.debt.ui.list.DebtListScreen
import com.driverwallet.app.feature.input.ui.QuickInputScreen
import com.driverwallet.app.feature.onboarding.OnboardingOverlay
import com.driverwallet.app.feature.report.ui.ReportScreen
import com.driverwallet.app.feature.settings.domain.SettingsKeys
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.feature.settings.ui.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showDebtForm by rememberSaveable { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val seen = settingsRepository.getSetting(SettingsKeys.HAS_SEEN_ONBOARDING)
        showOnboarding = seen != "true"
    }

    // Show nothing while checking onboarding status
    if (showOnboarding == null) return

    // Show onboarding overlay
    if (showOnboarding == true) {
        OnboardingOverlay(
            onFinish = {
                scope.launch {
                    settingsRepository.saveSetting(SettingsKeys.HAS_SEEN_ONBOARDING, "true")
                }
                showOnboarding = false
            },
        )
        return
    }

    // Main app
    if (showDebtForm) {
        DebtFormScreen(onBack = { showDebtForm = false })
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> QuickInputScreen()
                2 -> DebtListScreen(onAddDebt = { showDebtForm = true })
                3 -> ReportScreen()
                4 -> SettingsScreen()
            }
        }
    }
}
