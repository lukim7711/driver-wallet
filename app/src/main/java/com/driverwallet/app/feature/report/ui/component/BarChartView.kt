package com.driverwallet.app.feature.report.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.driverwallet.app.shared.domain.model.DailySummary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BarChartView(
    dailySummaries: List<DailySummary>,
    modifier: Modifier = Modifier,
    incomeColor: Color = MaterialTheme.colorScheme.primary,
    expenseColor: Color = MaterialTheme.colorScheme.error,
) {
    val maxValue = dailySummaries.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1) ?: 1
    val dayLabels = dailySummaries.map { summary ->
        LocalDate.parse(summary.date).dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale("id", "ID"))
    }

    val accessibilityDesc = buildString {
        append("Grafik batang. ")
        dailySummaries.forEach { day ->
            val parsed = LocalDate.parse(day.date)
            val name = parsed.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("id", "ID"))
            append("$name: masuk ${day.income}, keluar ${day.expense}. ")
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
            .semantics { contentDescription = accessibilityDesc },
    ) {
        val barGroupWidth = size.width / dailySummaries.size
        val barWidth = barGroupWidth * 0.3f
        val gap = barGroupWidth * 0.05f
        val chartHeight = size.height - 40f

        dailySummaries.forEachIndexed { index, summary ->
            val groupX = index * barGroupWidth + barGroupWidth * 0.15f

            val incomeHeight = (summary.income.toFloat() / maxValue) * chartHeight
            drawRoundRect(
                color = incomeColor,
                topLeft = Offset(groupX, chartHeight - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )

            val expenseHeight = (summary.expense.toFloat() / maxValue) * chartHeight
            drawRoundRect(
                color = expenseColor,
                topLeft = Offset(groupX + barWidth + gap, chartHeight - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )

            drawContext.canvas.nativeCanvas.drawText(
                dayLabels[index],
                groupX + barWidth,
                size.height,
                android.graphics.Paint().apply {
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.GRAY
                },
            )
        }
    }
}
