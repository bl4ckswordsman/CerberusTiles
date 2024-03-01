package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.widget.Toast
import kotlin.math.pow

/** Utilities for different settings. */
object SettingsUtils {
    /** Shows a toast with the given message. */
    fun showToast(context: Context, setting: String, isEnabled: Boolean) {
        val state = if (isEnabled) "enabled" else "disabled"
        Toast.makeText(context, "$setting $state", Toast.LENGTH_SHORT).show()
    }

    /** Utilities for brightness settings. */
    object Brightness {
        fun isAdaptiveBrightnessEnabled(context: Context): Boolean {
            return Settings.System.getInt(
                context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE
            ) == 1
        }

        fun toggleAdaptiveBrightness(context: Context) {
            if (Settings.System.canWrite(context)) {
                val isAdaptive = isAdaptiveBrightnessEnabled(context)
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    if (isAdaptive) 0 else 1
                )
                // Show a toast with the new state of adaptive brightness
                showToast(context, "Adaptive brightness", !isAdaptive)
            }
        }

        fun getScreenBrightness(context: Context): Int {
            return Settings.System.getInt(
                context.contentResolver, Settings.System.SCREEN_BRIGHTNESS
            )
        }

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

    object Vibration {
        fun isVibrationModeEnabled(context: Context): Boolean {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
        }

        fun toggleVibrationMode(context: Context) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                showToast(context, "Vibration mode", false)
            } else {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                showToast(context, "Vibration mode", true)
            }
        }
    }
}


// TODO: Add other settings utilities here
