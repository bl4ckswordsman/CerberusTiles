package com.bl4ckswordsman.cerberustiles

import android.app.Activity
import android.os.Bundle
import android.provider.Settings

/**
 * An [Activity] that toggles adaptive brightness when the user taps the adaptive brightness
 * shortcut.
 */
class ToggleAdaptiveBrightnessActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the app has the WRITE_SETTINGS permission
        if (Settings.System.canWrite(this)) {
            SettingsUtils.Brightness.toggleAdaptiveBrightness(this)
        }

        // Finish the activity without showing any UI
        finish()
    }
}