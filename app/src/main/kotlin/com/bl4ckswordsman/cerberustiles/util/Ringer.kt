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
                // For silent mode, use AutomaticZenManager for Android 15+ compatibility
                if (newMode == RingerMode.SILENT) {
                    val success = AutomaticZenManager.activateSilentMode(params.context)
                    if (!success) {
                        return // AutomaticZenManager already handles error messaging and permission requests
                    }
                    
                    // Set the audio manager to silent mode as well
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                } else {
                    // For normal and vibrate modes, deactivate silent mode first if it was active
                    if (AutomaticZenManager.isSilentModeActive(params.context)) {
                        AutomaticZenManager.deactivateSilentMode(params.context)
                    }
                    
                    // Set the desired mode
                    val systemMode = when (newMode) {
                        RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
                        RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                        RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT // Should not reach here
                    }
                    audioManager.ringerMode = systemMode
                }
                
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
