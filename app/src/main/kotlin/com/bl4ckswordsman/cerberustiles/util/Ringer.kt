package com.bl4ckswordsman.cerberustiles.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.RingerMode

object Ringer {
    fun getCurrentRingerMode(context: Context): RingerMode {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> RingerMode.NORMAL
            AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
            else -> RingerMode.NORMAL
        }
    }

    fun setRingerMode(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode) {
        // Check if we have write settings permission
        if (!SettingsUtils.canWriteSettings(params.context)) {
            SettingsUtils.openPermissionSettings(params.context)
            return
        }

        val audioManager = params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentMode = getCurrentRingerMode(params.context)
        
        println("Debug - Current mode: $currentMode, Attempting to set: $newMode")
        
        // Only proceed if the new mode is different from the current mode
        if (currentMode != newMode) {
            try {
                // For silent mode, we need DND permission and enable DND first
                if (newMode == RingerMode.SILENT) {
                    val notificationManager = params.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    // Check if we have DND permission
                    if (!notificationManager.isNotificationPolicyAccessGranted) {
                        Toast.makeText(
                            params.context,
                            "Silent mode requires Do Not Disturb permission. Redirecting to settings...",
                            Toast.LENGTH_SHORT
                        ).show()
                        SettingsUtils.openDndPermissionSettings(params.context)
                        return
                    }
                    
                    // Enable Do Not Disturb (INTERRUPTION_FILTER_NONE means complete silence)
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                    
                    // Small delay to ensure DND is activated
                    Thread.sleep(100)
                }
                
                // Temporarily set to normal to prevent DND activation (except for silent mode)
                if (newMode != RingerMode.SILENT) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
                
                // Then set to the desired mode
                val systemMode = when (newMode) {
                    RingerMode.NORMAL -> {
                        // When switching to normal, also disable DND
                        val notificationManager = params.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        if (notificationManager.isNotificationPolicyAccessGranted) {
                            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                        }
                        AudioManager.RINGER_MODE_NORMAL
                    }
                    RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT
                    RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                }
                audioManager.ringerMode = systemMode
                
                // Verify the change was successful
                val updatedMode = getCurrentRingerMode(params.context)
                println("Debug - Mode after change: $updatedMode")
                
                if (updatedMode == newMode) {
                    // Show toast with the mode name
                    val modeName = when(newMode) {
                        RingerMode.NORMAL -> "Sound mode"
                        RingerMode.SILENT -> "Silent mode"
                        RingerMode.VIBRATE -> "Vibrate mode"
                    }
                    SettingsUtils.showToast(params.context, modeName, true)
                    params.onSettingChanged(true)
                    println("Debug - Successfully changed mode and updated UI")
                } else {
                    println("Debug - Failed to change mode!")
                }
            } catch (e: SecurityException) {
                println("Error changing mode: ${e.message}")
                // Handle the specific case of DND restrictions
                if (newMode == RingerMode.SILENT) {
                    Toast.makeText(
                        params.context,
                        "Cannot set silent mode. Please grant Do Not Disturb permission in settings.",
                        Toast.LENGTH_LONG
                    ).show()
                    // Open DND permission settings
                    SettingsUtils.openDndPermissionSettings(params.context)
                } else {
                    Toast.makeText(
                        params.context,
                        "Cannot change ringer mode: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            println("Debug - No change needed, modes are the same")
        }
    }
}
