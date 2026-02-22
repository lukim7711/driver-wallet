package com.driverwallet.app.feature.input.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.core.ui.theme.ExpenseRed
import com.driverwallet.app.core.ui.theme.IncomeGreen

@Composable
fun TypeToggle(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TransactionType.entries.forEach { type ->
            val isSelected = type == selectedType
            val label = when (type) {
                TransactionType.INCOME -> "MASUK"
                TransactionType.EXPENSE -> "KELUAR"
            }
            Button(
                onClick = { onTypeSelected(type) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isSelected && type == TransactionType.INCOME -> IncomeGreen
                        isSelected && type == TransactionType.EXPENSE -> ExpenseRed
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (isSelected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
