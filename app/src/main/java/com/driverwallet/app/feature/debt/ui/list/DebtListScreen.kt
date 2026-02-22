package com.driverwallet.app.feature.debt.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driverwallet.app.core.ui.component.EmptyState
import com.driverwallet.app.core.ui.component.LoadingIndicator
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.core.ui.util.ObserveAsEvents
import com.driverwallet.app.feature.debt.domain.DebtWithScheduleInfo
import com.driverwallet.app.feature.debt.domain.model.DebtType
import com.driverwallet.app.feature.debt.ui.list.component.AddKasbonDialog
import com.driverwallet.app.feature.debt.ui.list.component.DebtCardItem
import com.driverwallet.app.feature.debt.ui.list.component.DebtHeroCard
import com.driverwallet.app.feature.debt.ui.list.component.FlexiblePaymentBottomSheet
import com.driverwallet.app.feature.debt.ui.list.component.KasbonHistorySheet
import com.driverwallet.app.feature.debt.ui.list.component.PaymentBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtListScreen(
    onAddDebt: () -> Unit = {},
    viewModel: DebtListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paymentDialog by viewModel.paymentDialog.collectAsStateWithLifecycle()
    val flexiblePaymentDialog by viewModel.flexiblePaymentDialog.collectAsStateWithLifecycle()
    val kasbonHistory by viewModel.kasbonHistoryState.collectAsStateWithLifecycle()
    val showAddKasbon by viewModel.showAddKasbon.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is GlobalUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDebt) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Hutang")
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = uiState) {
                is DebtListUiState.Loading -> LoadingIndicator()
                is DebtListUiState.Empty -> {
                    EmptyState(
                        icon = Icons.Outlined.CreditCard,
                        message = "Belum ada hutang.\nTekan + untuk menambahkan.",
                    )
                }
                is DebtListUiState.Success -> {
                    DebtListContent(
                        state = state,
                        onAction = viewModel::onAction,
                    )
                }
            }
        }
    }

    // Installment payment BottomSheet
    paymentDialog?.let { dialog ->
        PaymentBottomSheet(
            state = dialog,
            onConfirm = { amount ->
                viewModel.onAction(
                    DebtListUiAction.ConfirmPayment(
                        debtId = dialog.debtId,
                        scheduleId = dialog.scheduleId,
                        amount = amount,
                    ),
                )
            },
            onDismiss = { viewModel.onAction(DebtListUiAction.DismissPayment) },
        )
    }

    // Flexible payment BottomSheet (PERSONAL / TAB)
    flexiblePaymentDialog?.let { dialog ->
        FlexiblePaymentBottomSheet(
            state = dialog,
            onConfirm = { amount, note ->
                viewModel.onAction(
                    DebtListUiAction.ConfirmPayDebt(
                        debtId = dialog.debtId,
                        amount = amount,
                        note = note,
                    ),
                )
            },
            onDismiss = { viewModel.onAction(DebtListUiAction.DismissPayDebt) },
        )
    }

    // Kasbon history BottomSheet
    kasbonHistory?.let { history ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        KasbonHistorySheet(
            entries = history.entries,
            sheetState = sheetState,
            onDismiss = { viewModel.onAction(DebtListUiAction.DismissKasbonHistory) },
            onAddClick = { viewModel.onAction(DebtListUiAction.OpenAddKasbon(history.debtId)) },
        )
    }

    // Add kasbon dialog
    showAddKasbon?.let { debtId ->
        AddKasbonDialog(
            onDismiss = { viewModel.onAction(DebtListUiAction.DismissAddKasbon) },
            onConfirm = { amount, note ->
                viewModel.onAction(
                    DebtListUiAction.ConfirmAddKasbon(
                        debtId = debtId,
                        amount = amount,
                        note = note,
                    ),
                )
            },
        )
    }
}

@Composable
private fun DebtListContent(
    state: DebtListUiState.Success,
    onAction: (DebtListUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DebtHeroCard(
                totalRemaining = state.totalRemaining,
                hasOverdue = state.hasOverdue,
            )
        }
        items(
            items = state.debts,
            key = { it.debt.id },
        ) { info ->
            DebtCardItem(
                info = info,
                onPayClick = { handlePayClick(info, onAction) },
                onDeleteClick = { onAction(DebtListUiAction.DeleteDebt(info.debt.id)) },
            )
        }
    }
}

/** Route pay click based on debt type */
private fun handlePayClick(
    info: DebtWithScheduleInfo,
    onAction: (DebtListUiAction) -> Unit,
) {
    when (info.debt.debtType) {
        DebtType.INSTALLMENT -> {
            info.nextSchedule?.let { schedule ->
                onAction(DebtListUiAction.OpenPayment(info.debt.id, schedule))
            }
        }
        DebtType.PERSONAL -> {
            onAction(
                DebtListUiAction.OpenPayDebt(
                    debtId = info.debt.id,
                    debtName = info.debt.name,
                    remainingAmount = info.debt.remainingAmount,
                ),
            )
        }
        DebtType.TAB -> {
            onAction(
                DebtListUiAction.OpenPayDebt(
                    debtId = info.debt.id,
                    debtName = info.debt.name,
                    remainingAmount = info.debt.remainingAmount,
                ),
            )
        }
    }
}
