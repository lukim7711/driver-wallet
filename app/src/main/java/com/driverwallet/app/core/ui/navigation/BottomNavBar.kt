package com.driverwallet.app.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val hasBadge: Boolean = false,
)

@Composable
fun BottomNavBar(
    currentRoute: Any?,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier,
    hasDebtBadge: Boolean = false,
) {
    val items = listOf(
        BottomNavItem(
            route = DashboardRoute,
            label = "Beranda",
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
        ),
        BottomNavItem(
            route = QuickInputRoute,
            label = "Input",
            icon = Icons.Outlined.AddCircle,
            selectedIcon = Icons.Filled.AddCircle,
        ),
        BottomNavItem(
            route = DebtListRoute,
            label = "Hutang",
            icon = Icons.Outlined.Wallet,
            selectedIcon = Icons.Filled.Wallet,
            hasBadge = hasDebtBadge,
        ),
        BottomNavItem(
            route = ReportRoute,
            label = "Laporan",
            icon = Icons.Outlined.BarChart,
            selectedIcon = Icons.Filled.BarChart,
        ),
        BottomNavItem(
            route = SettingsRoute,
            label = "Pengaturan",
            icon = Icons.Outlined.Settings,
            selectedIcon = Icons.Filled.Settings,
        ),
    )

    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val isSelected = currentRoute?.let { it::class == item.route::class } ?: false
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    val imageVector = if (isSelected) item.selectedIcon else item.icon
                    if (item.hasBadge) {
                        BadgedBox(badge = { Badge() }) {
                            Icon(
                                imageVector = imageVector,
                                contentDescription = item.label,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = imageVector,
                            contentDescription = item.label,
                        )
                    }
                },
                label = { Text(item.label) },
            )
        }
    }
}
