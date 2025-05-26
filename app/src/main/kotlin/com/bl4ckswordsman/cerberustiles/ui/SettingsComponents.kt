package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val onRingerModeChange: (RingerMode) -> Unit,
    val isOverlayContext: Boolean = false
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
        RingerModeSelectionSegmentedButtonRow(
            currentMode = params.currentRingerMode,
            isOverlayContext = params.isOverlayContext,
            onModeSelected = { newMode ->
                RingerModeHandler(params, newMode).handleModeSelection()
            }
        )
    }
}

@Composable
private fun RingerModeSelectionSegmentedButtonRow(
    currentMode: RingerMode,
    isOverlayContext: Boolean = false,
    onModeSelected: (RingerMode) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ), // Padding to match other components' spacing
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(0.9f) // Scale to 90% width for better visual balance
        ) {
            RingerMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = currentMode == mode,
                    onClick = {
                        SegmentedButtonClickHandler(
                            context,
                            currentMode,
                            mode,
                            onModeSelected
                        ).handle()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, RingerMode.entries.size),
                    enabled = true, // Always enabled so clicks work for permission requests
                    icon = {
                        Icon(
                            painter = getIconForMode(mode, currentMode),
                            contentDescription = mode.name,
                            modifier = if (isOverlayContext) Modifier.size(16.dp) else Modifier
                        )
                    }
                ) {
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = if (isOverlayContext) {
                            LocalTextStyle.current.copy(fontSize = 12.sp)
                        } else {
                            LocalTextStyle.current
                        }
                    )
                }
            }
        }
    }
}

private fun triggerRingerModeChangeOnSelection(
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

/**
 * Handles ringer mode selection logic to reduce complexity.
 */
private class RingerModeHandler(
    private val params: SettingsComponentsParams,
    private val newMode: RingerMode
) {
    fun handleModeSelection() {
        if (params.canWriteState) {
            handleModeChangeWithPermission()
        } else {
            params.openPermissionSettings()
        }
    }

    private fun handleModeChangeWithPermission() {
        // Only process if we're selecting a different mode
        if (newMode != params.currentRingerMode) {
            updateVibrationMode()
            params.onRingerModeChange(newMode)
        }
    }

    private fun updateVibrationMode() {
        when (newMode) {
            RingerMode.VIBRATE -> {
                params.setVibrationMode(true)
            }

            RingerMode.NORMAL, RingerMode.SILENT -> {
                params.setVibrationMode(false)
            }
        }
    }
}

/**
 * Handles click events for segmented buttons to reduce complexity.
 */
private class SegmentedButtonClickHandler(
    private val context: android.content.Context,
    private val currentMode: RingerMode,
    private val selectedMode: RingerMode,
    private val onModeSelected: (RingerMode) -> Unit
) {
    fun handle() {
        if (currentMode != selectedMode) {
            if (!SettingsUtils.canWriteSettings(context)) {
                SettingsUtils.openPermissionSettings(context)
            } else {
                triggerRingerModeChangeOnSelection(context, selectedMode, onModeSelected)
            }
        }
    }
}

/**
 * Gets the appropriate icon for the given mode and current selection state.
 */
@Composable
private fun getIconForMode(mode: RingerMode, currentMode: RingerMode) = when (mode) {
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
