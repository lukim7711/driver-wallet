package com.driverwallet.app.feature.dashboard.ui.component

import androidx.compose.foundation.layout.Column
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
import com.driverwallet.app.core.ui.component.AppProgressBar
import com.driverwallet.app.core.util.CurrencyFormatter
import com.driverwallet.app.feature.dashboard.domain.model.BudgetInfo

@Composable
fun BudgetRemainingCard(
    budgetInfo: BudgetInfo,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Sisa Budget Hari Ini", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            AmountText(
                amount = budgetInfo.remaining,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppProgressBar(progress = budgetInfo.percentage)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Terpakai Rp ${CurrencyFormatter.format(budgetInfo.spentToday)} dari Rp ${CurrencyFormatter.format(budgetInfo.totalBudget)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
