package com.driverwallet.app.feature.debt.ui.list.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.ui.component.AmountText
import com.driverwallet.app.core.ui.component.HeroCard
import com.driverwallet.app.core.ui.theme.HeroAmountStyle

@Composable
fun DebtHeroCard(
    totalRemaining: Long,
    hasOverdue: Boolean,
    modifier: Modifier = Modifier,
) {
    HeroCard(modifier = modifier) {
        Text(
            text = "Total Sisa Hutang",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            AmountText(
                amount = totalRemaining,
                style = HeroAmountStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (hasOverdue) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.error,
                ) {
                    Text(
                        text = "\u26A0 Jatuh Tempo",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
