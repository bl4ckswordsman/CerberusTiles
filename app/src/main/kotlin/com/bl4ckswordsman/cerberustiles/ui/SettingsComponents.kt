package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext

/**
 * Parameters for controlling the visibility of settings components.
 */
data class ComponentVisibilityDialogParams(
    val adaptBrightnessSwitch: MutableState<Boolean>,
    val brightnessSlider: MutableState<Boolean>,
    val vibrationSwitch: MutableState<Boolean>
)

/**
 * Parameters for the settings components.
 */
data class SettingsComponentsParams(
    val componentVisibilityParams: ComponentVisibilityDialogParams,
    val canWriteState: Boolean,
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
 * A composable that shows settings components.
 */
@Composable
fun SettingsComponents(params: SettingsComponentsParams) {
    if (params.componentVisibilityParams.adaptBrightnessSwitch.value) {
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

    if (params.componentVisibilityParams.brightnessSlider.value) {
        BrightnessSlider(context = LocalContext.current)
    }

    if (params.componentVisibilityParams.vibrationSwitch.value) {
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