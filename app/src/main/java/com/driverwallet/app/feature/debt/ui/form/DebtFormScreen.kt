package com.driverwallet.app.feature.debt.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driverwallet.app.core.ui.component.LoadingIndicator
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.debt.domain.model.DebtType
import com.driverwallet.app.feature.debt.domain.model.PenaltyType

private val platforms = listOf("Shopee", "GoPay", "Kredivo", "SeaBank", "Akulaku", "Lainnya")
private val penaltyTypeOptions = listOf(
    PenaltyType.NONE to "Tidak Ada",
    PenaltyType.FIXED to "Nominal Tetap",
    PenaltyType.PERCENTAGE to "Persentase",
)
private val merchantTypeOptions = listOf("Warung", "Bengkel", "Toko", "Laundry", "Lainnya")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtFormScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
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
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState) {
                            is DebtFormUiState.TypeSelection -> "Pilih Jenis Hutang"
                            is DebtFormUiState.Ready.Installment -> "Tambah Cicilan"
                            is DebtFormUiState.Ready.Personal -> "Tambah Hutang Pribadi"
                            is DebtFormUiState.Ready.Tab -> "Tambah Kasbon"
                            else -> "Tambah Hutang"
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState is DebtFormUiState.Ready) {
                                viewModel.onAction(DebtFormUiAction.BackToTypeSelection)
                            } else {
                                onBack()
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is DebtFormUiState.Loading -> LoadingIndicator()
            is DebtFormUiState.TypeSelection -> {
                TypeSelectionContent(
                    onSelectType = { viewModel.onAction(DebtFormUiAction.SelectType(it)) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is DebtFormUiState.Ready.Installment -> {
                InstallmentFormContent(
                    state = state,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is DebtFormUiState.Ready.Personal -> {
                PersonalFormContent(
                    state = state,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is DebtFormUiState.Ready.Tab -> {
                TabFormContent(
                    state = state,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

// ==================== Type Selection ====================

@Composable
private fun TypeSelectionContent(
    onSelectType: (DebtType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Pilih jenis hutang yang ingin ditambahkan:",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        DebtTypeCard(
            icon = Icons.Filled.CreditCard,
            title = "Cicilan Platform",
            description = "Shopee PayLater, Kredivo, Akulaku, dll. Punya jadwal cicilan tetap tiap bulan.",
            onClick = { onSelectType(DebtType.INSTALLMENT) },
        )
        DebtTypeCard(
            icon = Icons.Filled.People,
            title = "Hutang Pribadi",
            description = "Pinjam uang ke teman, saudara, atau kenalan. Bayar kapan bisa, jumlah flexible.",
            onClick = { onSelectType(DebtType.PERSONAL) },
        )
        DebtTypeCard(
            icon = Icons.Filled.Receipt,
            title = "Kasbon / Tab",
            description = "Hutang di warung, bengkel, dll. Bisa nambah hutang lagi dan bayar bertahap.",
            onClick = { onSelectType(DebtType.TAB) },
        )
    }
}

@Composable
private fun DebtTypeCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ==================== Installment Form ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstallmentFormContent(
    state: DebtFormUiState.Ready.Installment,
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
        SaveButton(canSave = state.canSave, isSaving = state.isSaving, onAction = onAction)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==================== Personal Form ====================

@Composable
private fun PersonalFormContent(
    state: DebtFormUiState.Ready.Personal,
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
            placeholder = { Text("cth: Pinjam untuk bensin") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.borrowerName,
            onValueChange = { onAction(DebtFormUiAction.UpdateBorrowerName(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nama Orang *") },
            placeholder = { Text("cth: Pak Budi") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.relationship,
            onValueChange = { onAction(DebtFormUiAction.UpdateRelationship(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Hubungan") },
            placeholder = { Text("cth: teman, saudara, tetangga") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.totalAmount,
            onValueChange = { onAction(DebtFormUiAction.UpdateTotalAmount(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Jumlah Pinjaman *") },
            prefix = { Text("Rp ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.agreedReturnDate,
            onValueChange = { onAction(DebtFormUiAction.UpdateAgreedReturnDate(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Target Tanggal Kembali") },
            placeholder = { Text("cth: 2026-03-15 (opsional)") },
            singleLine = true,
        )
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
        SaveButton(canSave = state.canSave, isSaving = state.isSaving, onAction = onAction)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==================== Tab/Kasbon Form ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabFormContent(
    state: DebtFormUiState.Ready.Tab,
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
            placeholder = { Text("cth: Kasbon Warung Bu Sari") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.merchantName,
            onValueChange = { onAction(DebtFormUiAction.UpdateMerchantName(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nama Tempat *") },
            placeholder = { Text("cth: Warung Bu Sari") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Merchant type dropdown
        var merchantExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = merchantExpanded,
            onExpandedChange = { merchantExpanded = it },
        ) {
            OutlinedTextField(
                value = state.merchantType.ifBlank { "Pilih jenis" },
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                label = { Text("Jenis Tempat") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = merchantExpanded) },
            )
            ExposedDropdownMenu(
                expanded = merchantExpanded,
                onDismissRequest = { merchantExpanded = false },
            ) {
                merchantTypeOptions.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            onAction(DebtFormUiAction.UpdateMerchantType(type))
                            merchantExpanded = false
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
            label = { Text("Kasbon Awal *") },
            prefix = { Text("Rp ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
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
        SaveButton(canSave = state.canSave, isSaving = state.isSaving, onAction = onAction)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==================== Shared Components ====================

@Composable
private fun SaveButton(
    canSave: Boolean,
    isSaving: Boolean,
    onAction: (DebtFormUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onAction(DebtFormUiAction.Save) },
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = canSave,
        shape = RoundedCornerShape(50),
    ) {
        if (isSaving) {
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
}
