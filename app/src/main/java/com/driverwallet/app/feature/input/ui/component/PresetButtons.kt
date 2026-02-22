package com.driverwallet.app.feature.input.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val presets = listOf(
    5_000L to "+5rb",
    10_000L to "+10rb",
    20_000L to "+20rb",
    50_000L to "+50rb",
    100_000L to "+100rb",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetButtons(
    onPresetSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        presets.forEach { (amount, label) ->
            AssistChip(
                onClick = { onPresetSelected(amount) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}
