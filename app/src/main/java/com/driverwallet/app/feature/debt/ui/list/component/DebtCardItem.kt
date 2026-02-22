package com.driverwallet.app.feature.debt.ui.list.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.ui.component.AmountText
import com.driverwallet.app.core.ui.component.AppProgressBar
import com.driverwallet.app.feature.debt.domain.DebtWithScheduleInfo

private val platformColors = mapOf(
    "shopee" to Color(0xFFEE4D2D),
    "gopay" to Color(0xFF00AED6),
    "kredivo" to Color(0xFFE8590C),
    "seabank" to Color(0xFF00B894),
    "akulaku" to Color(0xFFE91E63),
)

@Composable
fun DebtCardItem(
    info: DebtWithScheduleInfo,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Platform icon + Name + Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlatformDot(
                    platform = info.debt.platform,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = info.debt.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = info.debt.platform,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Remaining amount
            AmountText(
                amount = info.debt.remainingAmount,
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            AppProgressBar(progress = info.percentage)

            Spacer(modifier = Modifier.height(4.dp))

            // Installment info
            Text(
                text = "Cicilan ${info.paidCount} dari ${info.totalCount} \u2022 ${(info.percentage * 100).toInt()}% Lunas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Next due date
            info.nextSchedule?.let { schedule ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Jatuh tempo: ${schedule.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (schedule.isOverdue) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Hapus")
                }
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.weight(1f),
                    enabled = info.nextSchedule != null,
                ) {
                    Text("Bayar")
                }
            }
        }
    }
}

@Composable
private fun PlatformDot(
    platform: String,
    modifier: Modifier = Modifier,
) {
    val color = platformColors[platform.lowercase()] ?: Color.Gray
    Box(
        modifier = modifier
            .background(color = color, shape = CircleShape),
    )
}
