package com.driverwallet.app.feature.report.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.ui.component.AmountText
import com.driverwallet.app.core.ui.theme.ExpenseColor
import com.driverwallet.app.core.ui.theme.IncomeColor

@Composable
fun SummaryCards(
    totalIncome: Long,
    totalExpense: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryCard(
            label = "Pemasukan",
            amount = totalIncome,
            isIncome = true,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            label = "Pengeluaran",
            amount = totalExpense,
            isIncome = false,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Long,
    isIncome: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            AmountText(
                amount = amount,
                style = MaterialTheme.typography.titleMedium,
                color = if (isIncome) IncomeColor else ExpenseColor,
            )
        }
    }
}
