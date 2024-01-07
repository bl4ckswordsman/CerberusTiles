package com.bl4ckswordsman.cerberustiles.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bl4ckswordsman.cerberustiles.MainActivity.Companion.HOME_SCREEN
import com.bl4ckswordsman.cerberustiles.MainActivity.Companion.SETTINGS_SCREEN

/**
 * The bottom navigation bar of the app.
 *
 * @param selectedScreen The currently selected screen.
 * @param onScreenSelected Function to call when a screen is selected.
 */
@Composable
fun BottomNavBar(selectedScreen: String, onScreenSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(selected = selectedScreen == HOME_SCREEN,
            onClick = { onScreenSelected(HOME_SCREEN) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(HOME_SCREEN) })
        NavigationBarItem(selected = selectedScreen == SETTINGS_SCREEN,
            onClick = { onScreenSelected(SETTINGS_SCREEN) },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            label = { Text(SETTINGS_SCREEN) })
    }
}