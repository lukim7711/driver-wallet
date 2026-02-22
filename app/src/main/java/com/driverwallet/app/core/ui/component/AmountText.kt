package com.driverwallet.app.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.driverwallet.app.core.util.CurrencyFormatter

@Composable
fun AmountText(
    amount: Long,
    modifier: Modifier = Modifier,
    prefix: String = "Rp ",
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = Color.Unspecified,
) {
    Text(
        text = "$prefix${CurrencyFormatter.format(amount)}",
        modifier = modifier,
        style = style,
        color = color,
    )
}
