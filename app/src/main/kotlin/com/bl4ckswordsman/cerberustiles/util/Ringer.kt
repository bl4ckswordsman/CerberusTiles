package com.bl4ckswordsman.cerberustiles.util

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
        if (currentMode == newMode) return

        val modeChangeResult = RingerModeChanger(params, newMode).execute()
        if (modeChangeResult.success) {
            params.onSettingChanged(true)
        }
    }

    /**
     * Handles the process of changing ringer mode with proper error handling.
     */
    private class RingerModeChanger(
        private val params: SettingsUtils.SettingsToggleParams,
        private val newMode: RingerMode
    ) {
        data class ChangeResult(val success: Boolean, val message: String? = null)

        fun execute(): ChangeResult {
            return try {
                applyRingerModeChange()
                verifyAndNotifyModeChange()
            } catch (e: SecurityException) {
                handleSecurityException(e)
                ChangeResult(false, e.message)
            }
        }

        private fun applyRingerModeChange() {
            val audioManager =
                params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            when (newMode) {
                RingerMode.SILENT -> applySilentMode(audioManager)
                else -> applyNonSilentMode(audioManager)
            }
        }

        private fun applySilentMode(audioManager: AudioManager) {
            val success = AutomaticZenManager.activateSilentMode(params.context)
            if (success) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
        }

        private fun applyNonSilentMode(audioManager: AudioManager) {
            if (AutomaticZenManager.isSilentModeActive(params.context)) {
                AutomaticZenManager.deactivateSilentMode(params.context)
            }

            val systemMode = when (newMode) {
                RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
                RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT
            }
            audioManager.ringerMode = systemMode
        }

        private fun verifyAndNotifyModeChange(): ChangeResult {
            val updatedMode = getCurrentRingerMode(params.context)

            return if (updatedMode == newMode) {
                val modeName = getModeDisplayName(newMode)
                SettingsUtils.showToast(params.context, modeName, true)
                ChangeResult(true)
            } else {
                ChangeResult(false, "Mode change verification failed")
            }
        }

        private fun handleSecurityException(e: SecurityException) {
            when (newMode) {
                RingerMode.SILENT -> showSilentModeError()
                else -> showGeneralError(e.message)
            }
        }

        private fun showSilentModeError() {
            Toast.makeText(
                params.context,
                "Cannot set silent mode. Please grant Do Not Disturb permission in settings.",
                Toast.LENGTH_LONG
            ).show()
            SettingsUtils.openDndPermissionSettings(params.context)
        }

        private fun showGeneralError(message: String?) {
            Toast.makeText(
                params.context,
                "Cannot change ringer mode: $message",
                Toast.LENGTH_LONG
            ).show()
        }

        private fun getModeDisplayName(mode: RingerMode): String {
            return when (mode) {
                RingerMode.NORMAL -> "Sound mode"
                RingerMode.SILENT -> "Silent mode"
                RingerMode.VIBRATE -> "Vibrate mode"
            }
        }
    }
}
