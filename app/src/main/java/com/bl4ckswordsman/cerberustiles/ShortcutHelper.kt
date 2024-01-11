package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class to create shortcuts.
 */
class ShortcutHelper(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    /**
     * Creates a shortcut with the given parameters.
     * @param id The ID of the shortcut.
     * @param shortLabel The short label of the shortcut.
     * @param longLabel The long label of the shortcut.
     * @param icon The icon of the shortcut.
     * @param action The action of the shortcut.
     * @param activityClass The activity class of the shortcut.
     */
    suspend fun createShortcut(
        id: String,
        shortLabel: String,
        longLabel: String,
        icon: Int,
        action: String,
        activityClass: Class<*>
    ) {
        withContext(Dispatchers.IO) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)

            val shortcut = ShortcutInfo.Builder(context, id)
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIcon(Icon.createWithResource(context, icon))
                .setIntent(Intent(context, activityClass).apply {
                    this.action = action
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                .build()

            shortcutManager?.dynamicShortcuts = listOf(shortcut)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    /**
     * Creates a shortcut to toggle adaptive brightness using createShortcut().
     */
    suspend fun createAdaptiveBrightnessShortcut() {
        createShortcut(
            id = "toggle_adaptive_brightness",
            shortLabel = "Toggle Adaptive Brightness",
            longLabel = "Toggle Adaptive Brightness",
            icon = R.drawable.baseline_brightness_auto_24,
            action = "com.bl4ckswordsman.cerberustiles.TOGGLE_ADAPTIVE_BRIGHTNESS",
            activityClass = ToggleAdaptiveBrightnessActivity::class.java
        )
    }

    /**
     * Creates a shortcut to toggle vibration mode using createShortcut().
     */
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    suspend fun createVibrationModeShortcut() {
        createShortcut(
            "toggle_vibration_ringer_mode",
            "Toggle Vibration Ringer Mode",
            "Toggle Vibration Ringer Mode",
            R.drawable.baseline_vibration_24,
            "com.bl4ckswordsman.cerberustiles.TOGGLE_VIBRATION_RINGER_MODE",
            activityClass = ToggleVibrationModeActivity::class.java
        )
    }
}