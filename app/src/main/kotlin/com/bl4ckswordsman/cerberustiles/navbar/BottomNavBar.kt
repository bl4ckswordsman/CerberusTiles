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
import androidx.navigation.NavController
import com.bl4ckswordsman.cerberustiles.Constants as label


/**
 * The screens of the app.
 *
 * @param route The route of the screen used for navigation.
 */
sealed class Screen(val route: String) {
    /**
     * The home screen of the app.
     */
    data object Home : Screen(label.HOME_SCREEN)
    /**
     * The settings screen of the app.
     */
    data object Settings : Screen(label.SETTINGS_SCREEN)
}

/**
 * The bottom navigation bar of the app.
 *
 * @param navController The navigation controller of the app.
 */
@Composable
fun BottomNavBar(navController: NavController) {
    val screens = listOf(Screen.Home, Screen.Settings)
    val icons = mapOf(
        Screen.Home to (Icons.Filled.Home to Icons.Outlined.Home),
        Screen.Settings to (Icons.Filled.Settings to Icons.Outlined.Settings)
    )

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = navController.currentDestination?.route == screen.route,
                onClick = {
                    if (navController.currentDestination?.route != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(screen.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    val (filledIcon, outlinedIcon) = icons[screen]!!
                    if (navController.currentDestination?.route == screen.route) {
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