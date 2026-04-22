package com.example.cyclapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppBottomBar(
    selected: String,
    onHomeClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val navGreen = Color(0xFFB8CB6A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = navGreen,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(selected = selected == "home") {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = "Inicio",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onHomeClick() }
            )
        }

        BottomNavItem(selected = false) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Ubicación",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }

        BottomNavItem(selected = false) {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = "Cámara",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }

        BottomNavItem(selected = selected == "missions") {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Misiones",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onMissionsClick() }
            )
        }

        BottomNavItem(selected = selected == "profile") {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Perfil",
                tint = Color.Black,
                modifier = Modifier
                    .size(30.dp)
                    .clickable { onProfileClick() }
            )
        }
    }
}