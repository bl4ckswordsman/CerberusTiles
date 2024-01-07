package com.bl4ckswordsman.customtiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.customtiles.navbar.BottomNavBar

/**
 * The main screen of the app.
 *
 * @param canWrite Whether the app has the WRITE_SETTINGS permission.
 * @param isAdaptive Whether adaptive brightness is enabled.
 * @param toggleAdaptiveBrightness Function to toggle adaptive brightness.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(canWrite: Boolean, isAdaptive: Boolean, toggleAdaptiveBrightness: () -> Unit) {
    val (isSwitchedOn, setSwitchedOn) = remember { mutableStateOf(isAdaptive) }
    val (selectedScreen, setSelectedScreen) = remember { mutableStateOf("Home") }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = selectedScreen, textAlign = TextAlign.Center
            )
        })
    },
        bottomBar = { BottomNavBar(selectedScreen, setSelectedScreen) },
        content = { innerPadding ->
            when (selectedScreen) {
                "Home" -> {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .height(100.dp)
                    ) {
                        SwitchWithLabel(
                            isSwitchedOn = isSwitchedOn,
                            onCheckedChange = {
                                setSwitchedOn(it)
                                if (canWrite) toggleAdaptiveBrightness()
                            },
                            label = if (isSwitchedOn) "Adaptive Brightness is ON" else "Adaptive Brightness is OFF"
                        )
                    }
                }

                "Settings" -> SettingsScreen()
                else -> Text(text = "Screen not found")
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
        .clickable { onCheckedChange(!isSwitchedOn) }
        .padding(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label)
            Switch(checked = isSwitchedOn, onCheckedChange = { onCheckedChange(it) })
        }
    }
}

/**
 * A preview of the main screen.
 */
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(canWrite = true, toggleAdaptiveBrightness = {}, isAdaptive = true)
}