package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Parameters for the settings components.
 */
data class SettingsComponentsParams(
    val showAdaptiveBrightnessSwitch: Boolean = true,
    val showBrightnessSlider: Boolean = true,
    val showVibrationModeSwitch: Boolean = true,
    val canWriteState: Boolean,
    val isSwitchedOn: Boolean,
    val setSwitchedOn: (Boolean) -> Unit,
    val toggleAdaptiveBrightness: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val isVibrationModeOn: Boolean,
    val setVibrationMode: (Boolean) -> Unit,
    val toggleVibrationMode: () -> Boolean
)

/**
 * A composable that shows settings components.
 */
@Composable
fun SettingsComponents(params: SettingsComponentsParams) {
    if (params.showAdaptiveBrightnessSwitch) {
        SwitchWithLabel(
            isSwitchedOn = params.isSwitchedOn,
            onCheckedChange = {
                params.setSwitchedOn(it)
                if (params.canWriteState) {
                    params.toggleAdaptiveBrightness()
                } else {
                    params.openPermissionSettings()
                }
            },
            label = if (params.isSwitchedOn) "Adaptive Brightness is ON" else "Adaptive Brightness is OFF"
        )
    }
    if (params.showBrightnessSlider) {
        BrightnessSlider(context = LocalContext.current)
    }
    if (params.showVibrationModeSwitch) {
        SwitchWithLabel(
            isSwitchedOn = params.isVibrationModeOn,
            onCheckedChange = { isChecked ->
                if (params.canWriteState) {
                    val isToggled = params.toggleVibrationMode()
                    if (isToggled) {
                        params.setVibrationMode(isChecked)
                    }
                } else {
                    params.openPermissionSettings()
                }
            },
            label = if (params.isVibrationModeOn) "Vibration Mode is ON" else "Vibration Mode is OFF"
        )
    }
}
