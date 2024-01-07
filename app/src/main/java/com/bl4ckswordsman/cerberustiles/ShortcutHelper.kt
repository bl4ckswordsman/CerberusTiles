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
class ShortcutHelper(private val context: Context, private val shortcutManager: ShortcutManager) {

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    /**
     * Creates a shortcut to toggle adaptive brightness.
     */
    suspend fun createAdaptiveBrightnessShortcut() {
        withContext(Dispatchers.IO) {
            val shortcutManager =
                context.getSystemService<ShortcutManager>(ShortcutManager::class.java)

            val shortcut = ShortcutInfo.Builder(context, "toggle_adaptive_brightness")
                .setShortLabel("Toggle Adaptive Brightness")
                .setLongLabel("Toggle Adaptive Brightness")
                .setIcon(
                    Icon.createWithResource(
                        context,
                        R.drawable.baseline_brightness_auto_24
                    )
                ) // Replace with your icon
                .setIntent(Intent(context, ToggleAdaptiveBrightnessActivity::class.java).apply {
                    action = "com.bl4ckswordsman.cerberustiles.TOGGLE_ADAPTIVE_BRIGHTNESS"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                .build()

            shortcutManager?.dynamicShortcuts = listOf(shortcut)
        }
    }
}