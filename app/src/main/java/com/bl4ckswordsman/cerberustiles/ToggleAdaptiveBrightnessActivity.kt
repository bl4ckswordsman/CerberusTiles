package com.bl4ckswordsman.cerberustiles

import android.app.Activity
import android.os.Bundle
import android.provider.Settings


class ToggleAdaptiveBrightnessActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the app has the WRITE_SETTINGS permission
        if (Settings.System.canWrite(this)) {
            val resolver = contentResolver
            val isAdaptive =
                Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0)
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                if (isAdaptive == 1) 0 else 1
            )
        }

        // Finish the activity without showing any UI
        finish()
    }
}