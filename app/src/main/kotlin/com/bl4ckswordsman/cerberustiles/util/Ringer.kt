package com.bl4ckswordsman.cerberustiles.util

import android.content.Context
import android.media.AudioManager
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
        val audioManager = params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentMode = getCurrentRingerMode(params.context)
        
        // Only proceed if the new mode is different from the current mode
        if (currentMode != newMode) {
            val systemMode = when (newMode) {
                RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
                RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT // TODO: Fix implementation for Silent mode
                RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
            }
            audioManager.ringerMode = systemMode
            
            // Show toast with the mode name
            val modeName = when(newMode) {
                RingerMode.NORMAL -> "Sound mode"
                RingerMode.SILENT -> "Silent mode"
                RingerMode.VIBRATE -> "Vibrate mode"
            }
            SettingsUtils.showToast(params.context, modeName, true)
            params.onSettingChanged(true)
        }
    }
}
