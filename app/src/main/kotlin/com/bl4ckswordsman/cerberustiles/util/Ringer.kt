package com.bl4ckswordsman.cerberustiles.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.RingerMode

/**
 * Utility object for managing device ringer mode settings.
 * Handles mode changes with proper permission checking and DND integration.
 */
object Ringer {
    
    /**
     * Gets the current ringer mode from the device's audio manager.
     * 
     * @param context The application context
     * @return The current [RingerMode] (NORMAL, SILENT, or VIBRATE)
     */
    fun getCurrentRingerMode(context: Context): RingerMode {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> RingerMode.NORMAL
            AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
            else -> RingerMode.NORMAL
        }
    }

    /**
     * Sets the device ringer mode with proper permission checking and DND integration.
     * 
     * @param params The settings toggle parameters containing context and callbacks
     * @param newMode The desired [RingerMode] to set
     */
    fun setRingerMode(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode) {
        if (!SettingsUtils.canWriteSettings(params.context)) {
            SettingsUtils.openPermissionSettings(params.context)
            return
        }

        val currentMode = getCurrentRingerMode(params.context)
        
        if (currentMode != newMode) {
            try {
                applyRingerModeChangeWithAudioManager(params, newMode)
                verifyModeChangeAndNotifyUser(params, newMode)
            } catch (e: SecurityException) {
                handleRingerModeSecurityException(params, newMode, e)
            }
        }
    }

    private fun applyRingerModeChangeWithAudioManager(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode) {
        val audioManager = params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        when (newMode) {
            RingerMode.SILENT -> activateSilentModeWithDndIntegration(params, audioManager)
            else -> activateNonSilentModeWithDndDeactivation(params, audioManager, newMode)
        }
    }

    private fun activateSilentModeWithDndIntegration(params: SettingsUtils.SettingsToggleParams, audioManager: AudioManager) {
        val success = AutomaticZenManager.activateSilentMode(params.context)
        if (!success) {
            return // AutomaticZenManager handles error messaging
        }
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    private fun activateNonSilentModeWithDndDeactivation(params: SettingsUtils.SettingsToggleParams, audioManager: AudioManager, newMode: RingerMode) {
        if (AutomaticZenManager.isSilentModeActive(params.context)) {
            AutomaticZenManager.deactivateSilentMode(params.context)
        }
        
        val systemMode = when (newMode) {
            RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
            RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
            RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT // Should not reach here
        }
        audioManager.ringerMode = systemMode
    }

    private fun verifyModeChangeAndNotifyUser(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode) {
        val updatedMode = getCurrentRingerMode(params.context)
        
        if (updatedMode == newMode) {
            val modeName = when(newMode) {
                RingerMode.NORMAL -> "Sound mode"
                RingerMode.SILENT -> "Silent mode"
                RingerMode.VIBRATE -> "Vibrate mode"
            }
            SettingsUtils.showToast(params.context, modeName, true)
            params.onSettingChanged(true)
        }
    }

    private fun handleRingerModeSecurityException(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode, e: SecurityException) {
        when (newMode) {
            RingerMode.SILENT -> {
                Toast.makeText(
                    params.context,
                    "Cannot set silent mode. Please grant Do Not Disturb permission in settings.",
                    Toast.LENGTH_LONG
                ).show()
                SettingsUtils.openDndPermissionSettings(params.context)
            }
            else -> {
                Toast.makeText(
                    params.context,
                    "Cannot change ringer mode: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
