package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.util.Ringer

/**
 * Parameters for controlling the visibility of settings components in the dialog.
 */
data class ComponentVisibilityDialogParams(
    val adaptBrightnessSwitch: MutableState<Boolean>,
    val brightnessSlider: MutableState<Boolean>,
    // val vibrationSwitch: MutableState<Boolean>, // TODO: Delete this
    val ringerModeSelector: MutableState<Boolean>
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
    val sharedParams: SharedParams,
    val currentRingerMode: RingerMode,
    val onRingerModeChange: (RingerMode) -> Unit
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

    if (params.componentVisibilityParams.ringerModeSelector.value) {
        RingerModeSelector(
            currentMode = params.currentRingerMode,
            onModeSelected = { newMode ->
                if (params.canWriteState) {
                    // Only process if we're selecting a different mode
                    if (newMode != params.currentRingerMode) {
                        when (newMode) {
                            RingerMode.VIBRATE -> {
                                params.setVibrationMode(true)
                            }
                            RingerMode.NORMAL, RingerMode.SILENT -> {
                                params.setVibrationMode(false)
                            }
                        }
                        params.onRingerModeChange(newMode)
                    }
                } else {
                    params.openPermissionSettings()
                }
            },
            enabled = params.canWriteState
        )
    }
}

@Composable
private fun RingerModeSelector(
    currentMode: RingerMode,
    onModeSelected: (RingerMode) -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    println("Debug - RingerModeSelector currentMode: $currentMode")

    SingleChoiceSegmentedButtonRow {
        RingerMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = currentMode == mode,
                onClick = {
                    println("Debug - Button clicked: $mode, Current: $currentMode")
                    if (enabled && currentMode != mode) {
                        println("Debug - Attempting to change mode to: $mode")
                        kotlin.runCatching {
                            Ringer.setRingerMode(
                                SettingsUtils.SettingsToggleParams(
                                    context = context,
                                    onSettingChanged = { _ ->
                                        println("Debug - Callback triggered for mode: $mode")
                                        onModeSelected(mode)
                                    }
                                ),
                                mode
                            )
                        }.onFailure { e ->
                            println("Debug - Error changing mode: ${e.message}")
                        }
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = RingerMode.entries.size),
                enabled = enabled,
                icon = {
                    when (mode) {
                        RingerMode.NORMAL -> Icon(
                            painter = if (currentMode == RingerMode.NORMAL)
                                painterResource(id = R.drawable.baseline_volume_up_24)
                            else
                                painterResource(id = R.drawable.outline_volume_up_24),
                            contentDescription = "Sound mode",
                        )
                        RingerMode.SILENT -> Icon(
                            painter = if (currentMode == RingerMode.SILENT)
                                painterResource(id = R.drawable.baseline_volume_off_24)
                            else
                                painterResource(id = R.drawable.outline_volume_off_24),
                            contentDescription = "Silent mode",
                        )
                        RingerMode.VIBRATE -> Icon(
                            painter = if (currentMode == RingerMode.VIBRATE)
                                painterResource(id = R.drawable.twotone_vibration_24)
                            else
                                painterResource(id = R.drawable.baseline_vibration_24),
                            contentDescription = "Vibrate mode",
                        )
                    }
                }
            ) {
                Text(
                    text = when (mode) {
                        RingerMode.NORMAL -> "Sound"
                        RingerMode.SILENT -> "Silent"
                        RingerMode.VIBRATE -> "Vibrate"
                    }
                )
            }
        }
    }
}
