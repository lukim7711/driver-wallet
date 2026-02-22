package com.driverwallet.app.core.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute
@Serializable data object QuickInputRoute
@Serializable data object DebtListRoute
@Serializable data class DebtFormRoute(val debtId: Long? = null)
@Serializable data object ReportRoute
@Serializable data object SettingsRoute
