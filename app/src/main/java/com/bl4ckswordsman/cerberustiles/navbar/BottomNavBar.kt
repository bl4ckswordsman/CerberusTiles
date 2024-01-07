package com.bl4ckswordsman.cerberustiles.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
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
    val screens = listOf(Screen.Home, Screen.Settings)
    val icons = mapOf(
        Screen.Home to (Icons.Filled.Home to Icons.Outlined.Home),
        Screen.Settings to (Icons.Filled.Settings to Icons.Outlined.Settings)
    )

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = selectedScreen == screen,
                onClick = { onScreenSelected(screen) },
                icon = {
                    val (filledIcon, outlinedIcon) = icons[screen]!!
                    if (selectedScreen == screen) {
                        Icon(filledIcon, contentDescription = null)
                    } else {
                        Icon(outlinedIcon, contentDescription = null)
                    }
                },
                label = { Text(screen.route) }
            )
        }
    }
}