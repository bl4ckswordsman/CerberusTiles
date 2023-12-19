package com.bl4ckswordsman.customtiles.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(selectedScreen: String, onScreenSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(selected = selectedScreen == "Home",
            onClick = { onScreenSelected("Home") },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("Home") })
        NavigationBarItem(selected = selectedScreen == "Settings",
            onClick = { onScreenSelected("Settings") },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            label = { Text("Settings") })
    }
}