package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.provider.Settings
import android.widget.Toast

/** Utilities for different settings. */
object SettingsUtils {
    /** Utilities for brightness settings. */
    object Brightness {
        fun isAdaptiveBrightnessEnabled(context: Context): Boolean {
            return Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE
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
}

// TODO: Add other settings utilities here
