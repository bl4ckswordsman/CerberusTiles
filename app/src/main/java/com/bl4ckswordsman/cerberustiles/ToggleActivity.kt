package com.bl4ckswordsman.cerberustiles

import android.app.Activity
import android.os.Bundle
import android.provider.Settings

/**
 * An [Activity] that toggles a setting when the user taps the shortcut.
 */
abstract class BaseToggleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the app has the necessary permissions
        if (hasRequiredPermissions()) {
            performAction()
        }

        // Finish the activity without showing any UI
        finish()
    }

    abstract fun hasRequiredPermissions(): Boolean

    abstract fun performAction()
}

/**
 * An [Activity] that toggles vibration mode when the user taps the vibration mode shortcut.
 * It uses BaseToggleActivity as a base class.
 */
class ToggleAdaptiveBrightnessActivity : BaseToggleActivity() {
    override fun hasRequiredPermissions(): Boolean {
        return Settings.System.canWrite(this)
    }

    override fun performAction() {
        SettingsUtils.Brightness.toggleAdaptiveBrightness(this)
    }
}
