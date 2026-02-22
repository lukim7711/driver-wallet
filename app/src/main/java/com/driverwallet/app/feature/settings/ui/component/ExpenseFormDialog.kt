package com.driverwallet.app.feature.settings.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.driverwallet.app.feature.settings.ui.EditingExpense

@Composable
fun ExpenseFormDialog(
    expense: EditingExpense,
    onDismiss: () -> Unit,
    onSave: (EditingExpense) -> Unit,
) {
    var name by remember(expense) { mutableStateOf(expense.name) }
    var amount by remember(expense) { mutableStateOf(expense.amount) }
    val isEditing = expense.id != 0L
    val typeLabel = if (expense.isMonthly) "Bulanan" else "Harian"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (isEditing) "Edit Pengeluaran $typeLabel" else "Tambah Pengeluaran $typeLabel")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        amount = newValue.filter { it.isDigit() }
                    },
                    label = { Text("Jumlah") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        expense.copy(
                            name = name,
                            amount = amount,
                        ),
                    )
                },
                enabled = name.isNotBlank() && (amount.toLongOrNull() ?: 0L) > 0,
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
    )
}
