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
                applyRingerModeChange(params, newMode)
                verifyAndNotifyModeChange(params, newMode)
            } catch (e: SecurityException) {
                handleRingerModeError(params, newMode, e)
            }
        }
    }

    /**
     * Applies the ringer mode change using appropriate methods for each mode.
     */
    private fun applyRingerModeChange(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode) {
        val audioManager = params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        when (newMode) {
            RingerMode.SILENT -> activateSilentMode(params, audioManager)
            else -> activateNonSilentMode(params, audioManager, newMode)
        }
    }

    /**
     * Activates silent mode using DND integration.
     */
    private fun activateSilentMode(params: SettingsUtils.SettingsToggleParams, audioManager: AudioManager) {
        val success = AutomaticZenManager.activateSilentMode(params.context)
        if (!success) {
            return // AutomaticZenManager handles error messaging
        }
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    /**
     * Activates normal or vibrate mode, deactivating DND if needed.
     */
    private fun activateNonSilentMode(params: SettingsUtils.SettingsToggleParams, audioManager: AudioManager, newMode: RingerMode) {
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

    /**
     * Verifies the mode change was successful and notifies the UI.
     */
    private fun verifyAndNotifyModeChange(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode) {
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

    /**
     * Handles errors during ringer mode changes.
     */
    private fun handleRingerModeError(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode, e: SecurityException) {
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
