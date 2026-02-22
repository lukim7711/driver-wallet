package com.driverwallet.app.feature.dashboard.ui.component

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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.ui.component.AmountText
import com.driverwallet.app.core.ui.component.HeroCard
import com.driverwallet.app.core.ui.theme.ExpenseRed
import com.driverwallet.app.core.ui.theme.HeroAmountStyle
import com.driverwallet.app.core.ui.theme.IncomeGreen

@Composable
fun ProfitHeroCard(
    profit: Long,
    percentChange: Float?,
    modifier: Modifier = Modifier,
) {
    HeroCard(
        modifier = modifier.semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = "Keuntungan Hari Ini",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            AmountText(
                amount = profit,
                style = HeroAmountStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (percentChange != null) {
                Spacer(modifier = Modifier.width(8.dp))
                val isPositive = percentChange >= 0
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isPositive) IncomeGreen else ExpenseRed,
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${percentChange.toInt()}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
