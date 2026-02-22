package com.driverwallet.app.feature.dashboard.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.core.ui.component.AmountText
import com.driverwallet.app.core.ui.component.CategoryIcon
import com.driverwallet.app.core.ui.theme.ExpenseRed
import com.driverwallet.app.core.ui.theme.IncomeGreen
import com.driverwallet.app.shared.domain.model.Transaction

@Composable
fun TodayTransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Transaksi Hari Ini", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (transactions.isEmpty()) {
            Text(
                text = "Belum ada transaksi hari ini",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            transactions.forEachIndexed { index, transaction ->
                TransactionItem(transaction = transaction)
                if (index < transactions.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CategoryIcon(
            iconName = transaction.category?.iconName ?: "add",
            contentDescription = transaction.category?.label,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.category?.label ?: "Lainnya",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (transaction.note.isNotEmpty()) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        AmountText(
            amount = transaction.amount,
            style = MaterialTheme.typography.bodyMedium,
            color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
            prefix = if (transaction.type == TransactionType.INCOME) "+Rp " else "-Rp ",
        )
    }
}
