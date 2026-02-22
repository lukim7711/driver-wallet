package com.driverwallet.app.feature.debt.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driverwallet.app.core.ui.component.LoadingIndicator
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.debt.domain.model.PenaltyType

private val platforms = listOf("Shopee", "GoPay", "Kredivo", "SeaBank", "Akulaku", "Lainnya")
private val penaltyTypeOptions = listOf(
    PenaltyType.NONE to "Tidak Ada",
    PenaltyType.FIXED to "Nominal Tetap",
    PenaltyType.PERCENTAGE to "Persentase",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtFormScreen(
    onBack: () -> Unit = {},
    viewModel: DebtFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is GlobalUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.savedSuccessfully.collect { onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Hutang") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is DebtFormUiState.Loading -> LoadingIndicator()
            is DebtFormUiState.Ready -> {
                DebtFormContent(
                    state = state,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtFormContent(
    state: DebtFormUiState.Ready,
    onAction: (DebtFormUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { onAction(DebtFormUiAction.UpdateName(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nama Hutang *") },
            placeholder = { Text("cth: Shopee PayLater Januari") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Platform dropdown
        var platformExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = platformExpanded,
            onExpandedChange = { platformExpanded = it },
        ) {
            OutlinedTextField(
                value = state.platform,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                label = { Text("Platform") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
            )
            ExposedDropdownMenu(
                expanded = platformExpanded,
                onDismissRequest = { platformExpanded = false },
            ) {
                platforms.forEach { platform ->
                    DropdownMenuItem(
                        text = { Text(platform) },
                        onClick = {
                            onAction(DebtFormUiAction.UpdatePlatform(platform))
                            platformExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.totalAmount,
            onValueChange = { onAction(DebtFormUiAction.UpdateTotalAmount(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Total Hutang *") },
            prefix = { Text("Rp ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.installmentPerMonth,
            onValueChange = { onAction(DebtFormUiAction.UpdateInstallmentPerMonth(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cicilan per Bulan *") },
            prefix = { Text("Rp ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.installmentCount,
            onValueChange = { onAction(DebtFormUiAction.UpdateInstallmentCount(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Jumlah Cicilan *") },
            placeholder = { Text("cth: 6") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.dueDay,
            onValueChange = { onAction(DebtFormUiAction.UpdateDueDay(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tanggal Jatuh Tempo (1-31) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.interestRate,
            onValueChange = { onAction(DebtFormUiAction.UpdateInterestRate(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Bunga (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Penalty type dropdown
        var penaltyExpanded by remember { mutableStateOf(false) }
        val selectedPenaltyLabel = penaltyTypeOptions
            .firstOrNull { it.first == state.penaltyType }?.second ?: "Tidak Ada"
        ExposedDropdownMenuBox(
            expanded = penaltyExpanded,
            onExpandedChange = { penaltyExpanded = it },
        ) {
            OutlinedTextField(
                value = selectedPenaltyLabel,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                label = { Text("Tipe Denda") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = penaltyExpanded) },
            )
            ExposedDropdownMenu(
                expanded = penaltyExpanded,
                onDismissRequest = { penaltyExpanded = false },
            ) {
                penaltyTypeOptions.forEach { (penaltyType, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onAction(DebtFormUiAction.UpdatePenaltyType(penaltyType))
                            penaltyExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.note,
            onValueChange = { onAction(DebtFormUiAction.UpdateNote(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Catatan") },
            minLines = 2,
            maxLines = 3,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onAction(DebtFormUiAction.Save) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.canSave,
            shape = RoundedCornerShape(50),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Menyimpan...")
            } else {
                Text("Simpan Hutang", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
