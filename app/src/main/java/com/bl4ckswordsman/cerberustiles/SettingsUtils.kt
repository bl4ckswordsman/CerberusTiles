package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.provider.Settings

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
            }
        }
    }
}

// TODO: Add other settings utilities here
