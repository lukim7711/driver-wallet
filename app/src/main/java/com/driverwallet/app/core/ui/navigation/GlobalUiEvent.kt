package com.driverwallet.app.core.ui.navigation

sealed interface GlobalUiEvent {
    data class ShowSnackbar(val message: String) : GlobalUiEvent
    data class Navigate(val route: Any) : GlobalUiEvent
    data object NavigateBack : GlobalUiEvent
}
