package com.driverwallet.app.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SmokingRooms
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CategoryIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val icon = iconNameToVector(iconName)
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private fun iconNameToVector(name: String): ImageVector = when (name) {
    "shopping_cart" -> Icons.Outlined.ShoppingCart
    "star" -> Icons.Outlined.Star
    "favorite" -> Icons.Outlined.Favorite
    "emoji_events" -> Icons.Outlined.EmojiEvents
    "local_gas_station" -> Icons.Outlined.LocalGasStation
    "restaurant" -> Icons.Outlined.Restaurant
    "smoking_rooms" -> Icons.Outlined.SmokingRooms
    "phone_android" -> Icons.Outlined.PhoneAndroid
    "local_parking" -> Icons.Outlined.LocalParking
    "build" -> Icons.Outlined.Build
    "toll" -> Icons.Outlined.Toll
    "credit_card" -> Icons.Outlined.CreditCard
    "more_horiz" -> Icons.Outlined.MoreHoriz
    else -> Icons.Outlined.Add
}
