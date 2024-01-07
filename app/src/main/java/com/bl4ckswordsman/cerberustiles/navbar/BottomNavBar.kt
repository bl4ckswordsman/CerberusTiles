package com.bl4ckswordsman.cerberustiles.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bl4ckswordsman.cerberustiles.Constants as label


sealed class Screen(val route: String) {
    data object Home : Screen(label.HOME_SCREEN)
    data object Settings : Screen(label.SETTINGS_SCREEN)
}

/**
 * The bottom navigation bar of the app.
 *
 * @param selectedScreen The currently selected screen.
 * @param onScreenSelected Function to call when a screen is selected.
 */
@Composable
fun BottomNavBar(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar {
        NavigationBarItem(selected = selectedScreen is Screen.Home,
            onClick = { onScreenSelected(Screen.Home) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(Screen.Home.route) })
        NavigationBarItem(selected = selectedScreen is Screen.Settings,
            onClick = { onScreenSelected(Screen.Settings) },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            label = { Text(Screen.Settings.route) })
    }
}