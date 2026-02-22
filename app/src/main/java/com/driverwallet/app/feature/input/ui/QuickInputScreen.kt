package com.driverwallet.app.feature.input.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driverwallet.app.core.ui.component.LoadingIndicator
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.core.ui.util.ObserveAsEvents
import com.driverwallet.app.feature.input.ui.component.AmountDisplay
import com.driverwallet.app.feature.input.ui.component.CategoryGrid
import com.driverwallet.app.feature.input.ui.component.NoteInput
import com.driverwallet.app.feature.input.ui.component.NumberPad
import com.driverwallet.app.feature.input.ui.component.PresetButtons
import com.driverwallet.app.feature.input.ui.component.SaveButton
import com.driverwallet.app.feature.input.ui.component.TypeToggle

@Composable
fun QuickInputScreen(
    viewModel: QuickInputViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is GlobalUiEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar(event.message)
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is QuickInputUiState.Loading -> LoadingIndicator()
            is QuickInputUiState.Ready -> {
                QuickInputContent(
                    state = state,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun QuickInputContent(
    state: QuickInputUiState.Ready,
    onAction: (QuickInputUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        TypeToggle(
            selectedType = state.type,
            onTypeSelected = { onAction(QuickInputUiAction.SwitchType(it)) },
        )
        Spacer(modifier = Modifier.height(24.dp))
        AmountDisplay(displayAmount = state.displayAmount)
        Spacer(modifier = Modifier.height(16.dp))
        PresetButtons(
            onPresetSelected = { onAction(QuickInputUiAction.AddPreset(it)) },
        )
        Spacer(modifier = Modifier.height(16.dp))
        NumberPad(
            onDigit = { onAction(QuickInputUiAction.AppendDigit(it)) },
            onBackspace = { onAction(QuickInputUiAction.Backspace) },
        )
        Spacer(modifier = Modifier.height(24.dp))
        CategoryGrid(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onCategorySelected = { onAction(QuickInputUiAction.SelectCategory(it)) },
        )
        Spacer(modifier = Modifier.height(16.dp))
        NoteInput(
            note = state.note,
            onNoteChange = { onAction(QuickInputUiAction.UpdateNote(it)) },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SaveButton(
            canSave = state.canSave,
            isSaving = state.isSaving,
            onClick = { onAction(QuickInputUiAction.Save) },
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}
