package com.driverwallet.app.feature.dashboard.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.ui.component.AppProgressBar
import com.driverwallet.app.core.ui.theme.ExpenseRed
import com.driverwallet.app.core.ui.theme.IncomeGreen
import com.driverwallet.app.core.util.CurrencyFormatter
import com.driverwallet.app.feature.dashboard.domain.model.DailyTarget

@Composable
fun DailyTargetSection(
    dailyTarget: DailyTarget,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Target Harian", style = MaterialTheme.typography.titleSmall)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (dailyTarget.isOnTrack) IncomeGreen else ExpenseRed,
                ) {
                    Text(
                        text = if (dailyTarget.isOnTrack) "ON TRACK" else "OFF TRACK",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            AppProgressBar(progress = dailyTarget.percentage)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Rp ${CurrencyFormatter.format(dailyTarget.earnedAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Rp ${CurrencyFormatter.format(dailyTarget.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
