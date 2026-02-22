package com.driverwallet.app.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Lifecycle-aware event collector for one-shot UI events (Snackbar, navigation, etc.).
 *
 * Unlike LaunchedEffect(Unit) + collect, this properly handles configuration changes
 * by suspending collection when the lifecycle drops below STARTED.
 *
 * Usage:
 * ```
 * ObserveAsEvents(viewModel.uiEvent) { event ->
 *     when (event) {
 *         is GlobalUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
 *         is GlobalUiEvent.NavigateBack -> navController.popBackStack()
 *     }
 * }
 * ```
 */
@Composable
fun <T> ObserveAsEvents(
    flow: Flow<T>,
    onEvent: suspend (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(onEvent)
        }
    }
}
