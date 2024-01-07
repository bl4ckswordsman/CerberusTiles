package com.bl4ckswordsman.cerberustiles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class AdaptiveBrightnessReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.bl4ckswordsman.cerberustiles.TOGGLE_ADAPTIVE_BRIGHTNESS") {
            // Check if the app has the WRITE_SETTINGS permission
            if (Settings.System.canWrite(context)) {
                val isAdaptive = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE
                ) == 1
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    if (isAdaptive) 0 else 1
                )
            }
        }
    }
}