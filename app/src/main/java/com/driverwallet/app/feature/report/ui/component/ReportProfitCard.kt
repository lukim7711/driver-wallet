package com.driverwallet.app.feature.report.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.ui.component.AmountText
import com.driverwallet.app.core.ui.component.HeroCard
import com.driverwallet.app.core.ui.theme.HeroAmountStyle

@Composable
fun ReportProfitCard(
    profit: Long,
    label: String = "Total Keuntungan",
    modifier: Modifier = Modifier,
) {
    HeroCard(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(modifier = Modifier.height(8.dp))
        AmountText(
            amount = profit,
            style = HeroAmountStyle,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
