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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.ui.component.AmountText
import com.driverwallet.app.core.ui.component.AppProgressBar
import com.driverwallet.app.feature.debt.domain.DebtWithScheduleInfo
import com.driverwallet.app.feature.debt.domain.model.DebtType

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
            // Header: Type icon + Name + Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                DebtTypeIcon(
                    debtType = info.debt.debtType,
                    platform = info.debt.platform,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = info.debt.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = debtSubtitle(info),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DebtTypeBadge(debtType = info.debt.debtType)
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

            // Progress text (type-aware)
            Text(
                text = progressText(info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Next due date (installment only)
            if (info.debt.debtType == DebtType.INSTALLMENT) {
                info.nextSchedule?.let { schedule ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Jatuh tempo: ${schedule.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (schedule.isOverdue) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
                ) {
                    Text(
                        when (info.debt.debtType) {
                            DebtType.INSTALLMENT -> "Bayar"
                            DebtType.PERSONAL -> "Bayar"
                            DebtType.TAB -> "Bayar"
                        },
                    )
                }
            }
        }
    }
}

// ==================== Helper composables ====================

@Composable
private fun DebtTypeIcon(
    debtType: DebtType,
    platform: String,
    modifier: Modifier = Modifier,
) {
    when (debtType) {
        DebtType.INSTALLMENT -> {
            val color = platformColors[platform.lowercase()] ?: MaterialTheme.colorScheme.primary
            Box(
                modifier = modifier.background(color = color.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CreditCard,
                    contentDescription = "Cicilan",
                    tint = color,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        DebtType.PERSONAL -> {
            Box(
                modifier = modifier.background(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                    shape = CircleShape,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = "Hutang Pribadi",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        DebtType.TAB -> {
            Box(
                modifier = modifier.background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    shape = CircleShape,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = "Kasbon",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun DebtTypeBadge(
    debtType: DebtType,
    modifier: Modifier = Modifier,
) {
    val (label, color) = when (debtType) {
        DebtType.INSTALLMENT -> "Cicilan" to MaterialTheme.colorScheme.primary
        DebtType.PERSONAL -> "Pribadi" to MaterialTheme.colorScheme.tertiary
        DebtType.TAB -> "Kasbon" to MaterialTheme.colorScheme.secondary
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .background(color = color.copy(alpha = 0.12f), shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

private fun debtSubtitle(info: DebtWithScheduleInfo): String = when (info.debt.debtType) {
    DebtType.INSTALLMENT -> info.debt.platform
    DebtType.PERSONAL -> (info.debt.detail as? com.driverwallet.app.feature.debt.domain.model.DebtDetail.Personal)?.borrowerName ?: ""
    DebtType.TAB -> (info.debt.detail as? com.driverwallet.app.feature.debt.domain.model.DebtDetail.Tab)?.merchantName ?: ""
}

private fun progressText(info: DebtWithScheduleInfo): String = when (info.debt.debtType) {
    DebtType.INSTALLMENT -> "Cicilan ${info.paidCount} dari ${info.totalCount} \u2022 ${(info.percentage * 100).toInt()}% Lunas"
    DebtType.PERSONAL -> "${info.paidCount}x pembayaran \u2022 ${(info.debt.progressPercent * 100).toInt()}% Lunas"
    DebtType.TAB -> "${info.paidCount}x pembayaran \u2022 ${(info.debt.progressPercent * 100).toInt()}% Lunas"
}
