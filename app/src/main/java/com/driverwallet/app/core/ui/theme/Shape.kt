package com.driverwallet.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object AppShapeTokens {
    val HeroCard = RoundedCornerShape(28.dp)
    val DebtCard = RoundedCornerShape(24.dp)
    val Pill = RoundedCornerShape(50)
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}
