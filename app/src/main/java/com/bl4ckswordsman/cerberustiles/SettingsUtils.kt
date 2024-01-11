package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.widget.Toast

/** Utilities for different settings. */
object SettingsUtils {
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
                val newState = if (isAdaptive) "disabled" else "enabled"
                Toast.makeText(context, "Adaptive brightness $newState", Toast.LENGTH_SHORT).show()
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
            } else {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }
        }
    }
}

// TODO: Add other settings utilities here
