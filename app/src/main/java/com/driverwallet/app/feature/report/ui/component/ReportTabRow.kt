package com.driverwallet.app.feature.report.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.driverwallet.app.feature.report.ui.ReportTab

private val tabLabels = mapOf(
    ReportTab.WEEKLY to "Mingguan",
    ReportTab.MONTHLY to "Bulanan",
    ReportTab.CUSTOM to "Custom",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTabRow(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { contentDescription = "Tab laporan" },
    ) {
        ReportTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                shape = SegmentedButtonDefaults.itemShape(index, ReportTab.entries.size),
            ) {
                Text(
                    text = tabLabels[tab] ?: tab.name,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
