package com.driverwallet.app.core.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driverwallet.app.feature.dashboard.ui.DashboardScreen
import com.driverwallet.app.feature.debt.ui.form.DebtFormScreen
import com.driverwallet.app.feature.debt.ui.list.DebtListScreen
import com.driverwallet.app.feature.input.ui.QuickInputScreen
import com.driverwallet.app.feature.onboarding.OnboardingOverlay
import com.driverwallet.app.feature.report.ui.ReportScreen
import com.driverwallet.app.feature.settings.ui.SettingsScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    viewModel: AppNavigationViewModel = hiltViewModel(),
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showDebtForm by rememberSaveable { mutableStateOf(false) }
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    // Show nothing while checking onboarding status
    if (showOnboarding == null) return

    // Show onboarding overlay
    if (showOnboarding == true) {
        OnboardingOverlay(
            onFinish = { viewModel.markOnboardingComplete() },
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
