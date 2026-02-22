package com.driverwallet.app.feature.settings.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BudgetSection(
    budgetBbm: String,
    budgetMakan: String,
    budgetRokok: String,
    budgetPulsa: String,
    onBudgetChange: (category: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Budget Harian",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        BudgetField(label = "BBM", value = budgetBbm, category = "fuel", onChanged = onBudgetChange)
        BudgetField(label = "Makan", value = budgetMakan, category = "food", onChanged = onBudgetChange)
        BudgetField(label = "Rokok", value = budgetRokok, category = "cigarette", onChanged = onBudgetChange)
        BudgetField(label = "Pulsa/Data", value = budgetPulsa, category = "phone", onChanged = onBudgetChange)
    }
}

@Composable
private fun BudgetField(
    label: String,
    value: String,
    category: String,
    onChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            onChanged(category, newValue.filter { it.isDigit() })
        },
        label = { Text(label) },
        prefix = { Text("Rp ") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}
