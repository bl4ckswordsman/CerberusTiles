package com.bl4ckswordsman.cerberustiles.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.view.WindowCompat
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.SettingsUtils.Brightness
import com.bl4ckswordsman.cerberustiles.SettingsUtils.Vibration
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialog
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialogParams
import com.bl4ckswordsman.cerberustiles.ui.createSharedParams

/**
 * A [ComponentActivity] that shows an overlay dialog with settings components.
 */
class OverlayActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.updateCanWrite(this)
        viewModel.updateIsSwitchedOn(this)
        viewModel.updateIsVibrationModeOn(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // window.statusBarColor = Color.TRANSPARENT // Deprecated in API 35
        // window.navigationBarColor = Color.TRANSPARENT
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContent {
            val showOverlayDialog = rememberSaveable { mutableStateOf(true) }
            val params = OverlayDialogParams(
                showDialog = showOverlayDialog,
                onDismiss = { finish() },
                canWrite = viewModel.canWrite,
                isSwitchedOn = viewModel.isSwitchedOn.value,
                setSwitchedOn = { viewModel.isSwitchedOn.value = it },
                toggleAdaptiveBrightness = {
                    val params = SettingsUtils.ToggleSettingsParams(this) { newValue ->
                        viewModel.isSwitchedOn.value = newValue
                    }
                    Brightness.toggleAdaptiveBrightness(params)
                },
                openPermissionSettings = { SettingsUtils.openPermissionSettings(this) },
                isVibrationModeOn = viewModel.isVibrationModeOn.value,
                setVibrationMode = { viewModel.isVibrationModeOn.value = it },
                toggleVibrationMode = {
                    val params = SettingsUtils.ToggleSettingsParams(this) { newValue ->
                        viewModel.isVibrationModeOn.value = newValue
                    }
                    Vibration.toggleVibrationMode(params)
                },
                sharedParams = createSharedParams()
            )
            OverlayDialog(params)
        }
    }
}