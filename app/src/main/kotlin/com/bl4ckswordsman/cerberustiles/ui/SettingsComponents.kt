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

/**
 * A composable that shows a segmented button row for selecting the ringer mode.
 */
@Composable
private fun RingerModeSelector(
    currentMode: RingerMode,
    onModeSelected: (RingerMode) -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current

    SingleChoiceSegmentedButtonRow {
        RingerMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = currentMode == mode,
                onClick = {
                    if (enabled && currentMode != mode) {
                        changeRingerMode(context, mode, onModeSelected)
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index, RingerMode.entries.size),
                enabled = enabled,
                icon = {
                    val iconPainter = when (mode) {
                        RingerMode.NORMAL -> if (currentMode == mode)
                            painterResource(R.drawable.baseline_volume_up_24)
                        else
                            painterResource(R.drawable.outline_volume_up_24)

                        RingerMode.SILENT -> if (currentMode == mode)
                            painterResource(R.drawable.baseline_volume_off_24)
                        else
                            painterResource(R.drawable.outline_volume_off_24)

                        RingerMode.VIBRATE -> if (currentMode == mode)
                            painterResource(R.drawable.twotone_vibration_24)
                        else
                            painterResource(R.drawable.baseline_vibration_24)
                    }
                    Icon(painter = iconPainter, contentDescription = mode.name)
                }
            ) {
                Text(text = mode.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

/**
 * Changes the ringer mode.
 */
private fun changeRingerMode(
    context: android.content.Context,
    mode: RingerMode,
    onModeSelected: (RingerMode) -> Unit
) {
    runCatching {
        Ringer.setRingerMode(
            SettingsUtils.SettingsToggleParams(
                context = context,
                onSettingChanged = { _ ->
                    onModeSelected(mode)
                }
            ),
            mode
        )
    }.onFailure { e ->
        println("Error changing mode: ${e.message}")
    }
}
