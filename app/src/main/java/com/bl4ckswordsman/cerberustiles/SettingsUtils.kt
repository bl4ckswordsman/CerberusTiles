package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.pow

/** Utilities for different settings. */
object SettingsUtils {
    /**
     * Parameters for toggling settings.
     */
    data class ToggleSettingsParams(
        val context: Context,
        val setVibrationMode: (Boolean) -> Unit
    )

    /**
     * Checks if the app can write settings.
     */
    fun canWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    /**
     * Shows a toast with the given message.
     */
    fun showToast(context: Context, setting: String, isEnabled: Boolean) {
        val state = if (isEnabled) "enabled" else "disabled"
        Toast.makeText(context, "$setting $state", Toast.LENGTH_SHORT).show()
    }

    /**
     * Utilities for brightness settings.
     */
    object Brightness {
        /**
         * Checks if the adaptive brightness is enabled.
         */
        fun isAdaptiveBrightnessEnabled(context: Context): Boolean {
            return Settings.System.getInt(
                context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE
            ) == 1
        }

        /**
         * Toggles the adaptive brightness setting.
         */
        fun toggleAdaptiveBrightness(params: ToggleSettingsParams) {
            if (Settings.System.canWrite(params.context)) {
                val isAdaptive = isAdaptiveBrightnessEnabled(params.context)
                Settings.System.putInt(
                    params.context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    if (isAdaptive) 0 else 1
                )
                // Show a toast with the new state of adaptive brightness
                showToast(params.context, "Adaptive brightness", !isAdaptive)
            }
        }

        /**
         * Gets the screen brightness.
         */
        fun getScreenBrightness(context: Context): Int {
            return Settings.System.getInt(
                context.contentResolver, Settings.System.SCREEN_BRIGHTNESS
            )
        }

        /**
         * Sets the screen brightness.
         */
        fun setScreenBrightness(context: Context, brightness: Float) {
            if (Settings.System.canWrite(context)) {
                // Convert the brightness value to a 0-255 range
                val brightnessValue = (255.0.pow(brightness.toDouble())).toInt()
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
                )
            }
        }
    }

    /**
     * Utilities for vibration settings.
     */
    object Vibration {
        /**
         * Checks if the vibration mode is enabled.
         */
        fun isVibrationModeEnabled(context: Context): Boolean {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
        }

        /**
         * Toggles the vibration mode.
         */
        fun toggleVibrationMode(params: ToggleSettingsParams): Boolean {
            val audioManager = params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return try {
                val isVibrationModeOn = audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
                if (isVibrationModeOn) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    showToast(params.context, "Vibration mode", false)
                } else {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    showToast(params.context, "Vibration mode", true)
                }
                params.setVibrationMode(!isVibrationModeOn)
                true
            } catch (e: SecurityException) { // Catch the exception when the app is in DND mode
                Toast.makeText(params.context, "Cannot change vibration settings in Do Not Disturb mode",
                    Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    /**
     * Opens the screen to allow the user to write system settings.
     */
    fun openPermissionSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

/**
 * The main view model that holds the state of the settings.
 */
class MainViewModel : ViewModel() {
    val canWrite = MutableLiveData<Boolean>()
    val isSwitchedOn = mutableStateOf(false)
    val isVibrationModeOn = mutableStateOf(false)

    fun updateCanWrite(context: Context) {
        canWrite.value = SettingsUtils.canWriteSettings(context)
    }

    fun updateIsSwitchedOn(context: Context) {
        isSwitchedOn.value = SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(context)
    }

    fun updateIsVibrationModeOn(context: Context) {
        isVibrationModeOn.value = SettingsUtils.Vibration.isVibrationModeEnabled(context)
    }
}


// TODO: Add other settings utilities here
