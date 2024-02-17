package com.bl4ckswordsman.cerberustiles

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bl4ckswordsman.cerberustiles.navbar.BottomNavBar
import com.bl4ckswordsman.cerberustiles.navbar.Screen
import com.bl4ckswordsman.cerberustiles.Constants as label

/**
 * The main screen of the app.
 *
 * @param canWrite Whether the app has the WRITE_SETTINGS permission.
 * @param isAdaptive Whether adaptive brightness is enabled.
 * @param toggleAdaptiveBrightness Function to toggle adaptive brightness.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    canWrite: LiveData<Boolean>,
    isAdaptive: LiveData<Boolean>,
    toggleAdaptiveBrightness: () -> Unit,
    isVibrationMode: LiveData<Boolean>,
    toggleVibrationMode: () -> Unit,
    openPermissionSettings: () -> Unit
) {
    val navController = rememberNavController()
    val canWriteState by canWrite.observeAsState(initial = false)
    val isAdaptiveState by isAdaptive.observeAsState(initial = false)
    val isVibrationModeState by isVibrationMode.observeAsState(initial = false)

    val (isSwitchedOn, setSwitchedOn) = remember { mutableStateOf(isAdaptiveState) }
    val (isVibrationModeOn, setVibrationMode) = remember { mutableStateOf(isVibrationModeState) }

    SideEffect {
        setSwitchedOn(isAdaptiveState)
        setVibrationMode(isVibrationModeState)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedScreen = when (navBackStackEntry?.destination?.route) {
        Screen.Home.route -> Screen.Home
        Screen.Settings.route -> Screen.Settings
        else -> Screen.Home
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                //containerColor = MaterialTheme.colorScheme.secondaryContainer, // removing this causes a nice immersive effect
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text(
                    text = when (selectedScreen) {
                        is Screen.Home -> label.HOME_SCREEN
                        is Screen.Settings -> label.SETTINGS_SCREEN
                    }
                )
            }
        )
    },
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    SwitchWithLabel(
                        isSwitchedOn = isSwitchedOn,
                        onCheckedChange = {
                            setSwitchedOn(it)
                            if (canWriteState) {
                                toggleAdaptiveBrightness()
                            } else {
                                openPermissionSettings()
                            }
                        },
                        label = if (isSwitchedOn) "Adaptive Brightness is ON" else "Adaptive Brightness is OFF"
                    )
                    SwitchWithLabel(
                        isSwitchedOn = isVibrationModeOn,
                        onCheckedChange = {
                            setVibrationMode(it)
                            if (canWriteState) {
                                toggleVibrationMode()
                            } else {
                                openPermissionSettings()
                            }
                        },
                        label = if (isVibrationModeOn) "Vibration Mode is ON" else "Vibration Mode is OFF"
                    )
                }
            }

            composable(Screen.Settings.route) {
                SettingsScreen(innerPadding)
            }
        }
    }
}

/**
 * A switch with a label. The label is clickable and toggles the switch.
 */
@Composable
fun SwitchWithLabel(isSwitchedOn: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isSwitchedOn) }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label)
            Switch(checked = isSwitchedOn,
                onCheckedChange = { onCheckedChange(it) },
                thumbContent = if (isSwitchedOn) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                })
        }
    }
}

/**
 * A preview of the main screen.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(canWrite = MutableLiveData(true),
        toggleAdaptiveBrightness = {},
        isAdaptive = MutableLiveData(true),
        toggleVibrationMode = {},
        isVibrationMode = MutableLiveData(true),
        openPermissionSettings = {})
}
