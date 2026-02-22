package com.driverwallet.app.feature.settings.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.util.CurrencyFormatter
import com.driverwallet.app.feature.settings.ui.FixedExpenseDisplay

@Composable
fun FixedExpenseSection(
    title: String,
    expenses: List<FixedExpenseDisplay>,
    onAdd: () -> Unit,
    onEdit: (FixedExpenseDisplay) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            TextButton(onClick = onAdd) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Tambah",
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah")
            }
        }
        if (expenses.isEmpty()) {
            Text(
                text = "Belum ada data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            expenses.forEach { expense ->
                ExpenseItem(
                    expense = expense,
                    onEdit = { onEdit(expense) },
                    onDelete = { onDelete(expense.id) },
                )
            }
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: FixedExpenseDisplay,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onEdit,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Text(
                    text = when (expense.icon) {
                        "payments" -> "\uD83D\uDCB3"
                        "home" -> "\uD83C\uDFE0"
                        "wifi" -> "\uD83D\uDCF6"
                        "electric_bolt" -> "\u26A1"
                        "water_drop" -> "\uD83D\uDCA7"
                        else -> "\uD83D\uDCB0"
                    },
                    modifier = Modifier.padding(8.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Rp ${CurrencyFormatter.format(expense.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Hapus ${expense.name}",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
