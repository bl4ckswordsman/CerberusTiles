package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LiveData
import com.bl4ckswordsman.cerberustiles.ui.theme.CustomTilesTheme

/**
 * Parameters for the overlay dialog.
 */
data class OverlayDialogParams(
    val showDialog: MutableState<Boolean>,
    val onDismiss: () -> Unit,
    val canWrite: LiveData<Boolean>,
    val isSwitchedOn: Boolean,
    val setSwitchedOn: (Boolean) -> Unit,
    val toggleAdaptiveBrightness: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val isVibrationModeOn: Boolean,
    val setVibrationMode: (Boolean) -> Unit,
    val toggleVibrationMode: () -> Boolean,
    val sharedParams: SharedParams
)

/**
 * A dialog that shows settings components.
 */
@Composable
fun OverlayDialog(params: OverlayDialogParams) {
    val adaptBrightnessSwitch = params.sharedParams.sharedPreferences.getBoolean("adaptBrightnessSwitch", true)
    val brightnessSlider = params.sharedParams.sharedPreferences.getBoolean("brightnessSlider", true)
    val vibrationSwitch = params.sharedParams.sharedPreferences.getBoolean("vibrationSwitch", true)
    val canWriteState by params.canWrite.observeAsState(initial = false)
    if (params.showDialog.value) {
        Dialog(onDismissRequest = {
            params.showDialog.value = false
            params.onDismiss()
        }) {
            CustomTilesTheme {
                AlertDialog(
                    onDismissRequest = {
                        params.showDialog.value = false
                        params.onDismiss()
                    },
                    text = {
                        Column {
                            val componentVisibilityDialogParams = ComponentVisibilityDialogParams(
                                adaptBrightnessSwitch = rememberSaveable { mutableStateOf(adaptBrightnessSwitch) },
                                brightnessSlider = rememberSaveable { mutableStateOf(brightnessSlider) },
                                vibrationSwitch = rememberSaveable { mutableStateOf(vibrationSwitch) }
                            )
                            val settingsComponentsParams = SettingsComponentsParams(
                                canWriteState = canWriteState,
                                isSwitchedOn = params.isSwitchedOn,
                                setSwitchedOn = params.setSwitchedOn,
                                toggleAdaptiveBrightness = params.toggleAdaptiveBrightness,
                                openPermissionSettings = params.openPermissionSettings,
                                isVibrationModeOn = params.isVibrationModeOn,
                                setVibrationMode = params.setVibrationMode,
                                toggleVibrationMode = params.toggleVibrationMode,
                                componentVisibilityParams = componentVisibilityDialogParams,
                                sharedParams = createSharedParams()
                            )
                            SettingsComponents(settingsComponentsParams)
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            params.showDialog.value = false
                            params.onDismiss()
                        }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}