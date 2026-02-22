package com.driverwallet.app.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FinancePrimaryDark,
    onPrimary = FinanceOnPrimaryDark,
    primaryContainer = FinancePrimaryContainerDark,
    onPrimaryContainer = FinanceOnPrimaryContainerDark,
    secondary = FinanceSecondaryDark,
    onSecondary = FinanceOnSecondaryDark,
    secondaryContainer = FinanceSecondaryContainerDark,
    onSecondaryContainer = FinanceOnSecondaryContainerDark,
    tertiary = FinanceTertiaryDark,
    onTertiary = FinanceOnTertiaryDark,
    tertiaryContainer = FinanceTertiaryContainerDark,
    onTertiaryContainer = FinanceOnTertiaryContainerDark,
    error = FinanceErrorDark,
    onError = FinanceOnErrorDark,
    errorContainer = FinanceErrorContainerDark,
    onErrorContainer = FinanceOnErrorContainerDark,
    background = FinanceBackgroundDark,
    onBackground = FinanceOnBackgroundDark,
    surface = FinanceSurfaceDark,
    onSurface = FinanceOnSurfaceDark,
    surfaceVariant = FinanceSurfaceVariantDark,
    onSurfaceVariant = FinanceOnSurfaceVariantDark,
    outline = FinanceOutlineDark,
)

private val LightColorScheme = lightColorScheme(
    primary = FinancePrimaryLight,
    onPrimary = FinanceOnPrimaryLight,
    primaryContainer = FinancePrimaryContainerLight,
    onPrimaryContainer = FinanceOnPrimaryContainerLight,
    secondary = FinanceSecondaryLight,
    onSecondary = FinanceOnSecondaryLight,
    secondaryContainer = FinanceSecondaryContainerLight,
    onSecondaryContainer = FinanceOnSecondaryContainerLight,
    tertiary = FinanceTertiaryLight,
    onTertiary = FinanceOnTertiaryLight,
    tertiaryContainer = FinanceTertiaryContainerLight,
    onTertiaryContainer = FinanceOnTertiaryContainerLight,
    error = FinanceErrorLight,
    onError = FinanceOnErrorLight,
    errorContainer = FinanceErrorContainerLight,
    onErrorContainer = FinanceOnErrorContainerLight,
    background = FinanceBackgroundLight,
    onBackground = FinanceOnBackgroundLight,
    surface = FinanceSurfaceLight,
    onSurface = FinanceOnSurfaceLight,
    surfaceVariant = FinanceSurfaceVariantLight,
    onSurfaceVariant = FinanceOnSurfaceVariantLight,
    outline = FinanceOutlineLight,
)

@Composable
fun DriverWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
