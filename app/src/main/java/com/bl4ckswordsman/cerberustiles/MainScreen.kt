package com.bl4ckswordsman.cerberustiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    canWrite: LiveData<Boolean>,
    isAdaptive: LiveData<Boolean>,
    toggleAdaptiveBrightness: () -> Unit,
    openPermissionSettings: () -> Unit
) {
    val canWriteState by canWrite.observeAsState(initial = false)
    val isAdaptiveState by isAdaptive.observeAsState(initial = false)

    val (isSwitchedOn, setSwitchedOn) = remember { mutableStateOf(isAdaptiveState) }
    val (selectedScreen, setSelectedScreen) = remember { mutableStateOf<Screen>(Screen.Home) }

    SideEffect {
        setSwitchedOn(isAdaptiveState)
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer, // removing this causes a nice immersive effect
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
        bottomBar = { BottomNavBar(selectedScreen, setSelectedScreen) },
        content = { innerPadding ->
            when (selectedScreen) {
                is Screen.Home -> {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .height(100.dp)
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
                    }
                }

                is Screen.Settings -> SettingsScreen()
            }
        })
}

/**
 * A switch with a label. The label is clickable and toggles the switch.
 */
@Composable
fun SwitchWithLabel(isSwitchedOn: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)) {
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
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(canWrite = MutableLiveData(true),
        toggleAdaptiveBrightness = {},
        isAdaptive = MutableLiveData(true),
        openPermissionSettings = {})
}
