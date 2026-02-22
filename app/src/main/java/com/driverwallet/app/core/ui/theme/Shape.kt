package com.driverwallet.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DriverWalletShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp),
)

object AppShapes {
    val HeroCard = RoundedCornerShape(28.dp)
    val StandardCard = RoundedCornerShape(16.dp)
    val DebtCard = RoundedCornerShape(24.dp)
    val CategoryIcon = RoundedCornerShape(20.dp)
    val Pill = RoundedCornerShape(percent = 50)
    val FilledTextField = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
}
