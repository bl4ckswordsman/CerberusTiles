package com.bl4ckswordsman.cerberustiles.util

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.service.notification.Condition
import android.service.notification.AutomaticZenRule
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.activities.MainActivity // Ensured import
import com.bl4ckswordsman.cerberustiles.models.RingerMode

object Ringer {
    // 1. Constants
    private const val ACTUAL_RULE_ID = "com.bl4ckswordsman.cerberustiles.SILENT_MODE_RULE_DEFAULT"
    private const val AUTOMATIC_RULE_NAME = "CerberusTiles Silent Mode"
    private val DEFAULT_CONDITION_ID: Uri = Uri.parse("content://com.bl4ckswordsman.cerberustiles/zen_condition_id_default")

    fun getCurrentRingerMode(context: Context): RingerMode {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> RingerMode.NORMAL
            AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
            else -> RingerMode.NORMAL
        }
    }

    // 2. Helper Function getOrCreateAutomaticZenRule
    private fun getOrCreateAutomaticZenRule(context: Context, notificationManager: NotificationManager): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return null // Should not be called for older versions
        }

        try {
            if (notificationManager.getAutomaticZenRule(ACTUAL_RULE_ID) != null) {
                return ACTUAL_RULE_ID
            }

            // For AutomaticZenRule.Builder, the name is set via .setName()
            // The constructor is Builder(id, conditionId)
            val mainActivityComponent = ComponentName(context, MainActivity::class.java)
            val rule = AutomaticZenRule.Builder(ACTUAL_RULE_ID, DEFAULT_CONDITION_ID)
                .setName(AUTOMATIC_RULE_NAME) // Set name using builder method
                .setOwner(null) // Set owner to null
                .setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE) // Set interruption filter
                .setConfigurationActivity(mainActivityComponent) // Set configuration activity
                .setEnabled(true) // Set enabled to true
                .build()

            return notificationManager.addAutomaticZenRule(rule)
        } catch (e: SecurityException) {
            println("SecurityException in getOrCreateAutomaticZenRule: ${e.message}")
            // Toast and further actions are better handled by the calling function
            return null
        }
    }

    @Suppress("DEPRECATION") // For older SDK_INT checks and setInterruptionFilter
    fun setRingerMode(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode) {
        // Get AudioManager, NotificationManager early
        val audioManager = params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = params.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currentMode = getCurrentRingerMode(params.context)

        // Keep existing canWriteSettings check
        if (!SettingsUtils.canWriteSettings(params.context)) {
            SettingsUtils.openPermissionSettings(params.context)
            params.onSettingChanged(false)
            return
        }

        // Keep existing currentMode == newMode check
        if (currentMode == newMode) {
            println("Debug - No change needed, modes are the same.")
            params.onSettingChanged(true) // Report success as no change was needed
            return
        }

        // DND Permission Check (as specified)
        if (newMode == RingerMode.SILENT || (currentMode == RingerMode.SILENT && newMode != RingerMode.SILENT)) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                Toast.makeText(
                    params.context,
                    "Do Not Disturb permission is required. Redirecting to settings...",
                    Toast.LENGTH_LONG
                ).show()
                SettingsUtils.openDndPermissionSettings(params.context)
                params.onSettingChanged(false)
                return
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                // Android 15+ (API 35+) Logic
                if (newMode == RingerMode.SILENT) {
                    val ruleId = getOrCreateAutomaticZenRule(params.context, notificationManager)
                    if (ruleId == null) {
                        Toast.makeText(params.context, "Failed to create/access silent mode rule. DND permission might be missing or other error.", Toast.LENGTH_LONG).show()
                        params.onSettingChanged(false)
                        return
                    }
                    val condition = Condition(DEFAULT_CONDITION_ID, "", Condition.STATE_TRUE)
                    notificationManager.setAutomaticZenRuleState(ruleId, condition)
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT // Also set ringer mode
                    // Old setInterruptionFilter and Thread.sleep are implicitly removed for this path
                } else if (currentMode == RingerMode.SILENT) { // Disabling silent mode on Android 15+
                    val ruleId = getOrCreateAutomaticZenRule(params.context, notificationManager)
                    // If ruleId is null, it might mean it was never created or got deleted.
                    // We should still try to change the ringer mode away from silent.
                    if (ruleId == null) {
                         println("Warning: Could not get ruleId to disable AutomaticZenRule. Attempting direct ringer mode change.")
                    } else {
                        val condition = Condition(DEFAULT_CONDITION_ID, "", Condition.STATE_FALSE)
                        notificationManager.setAutomaticZenRuleState(ruleId, condition)
                    }
                    // Set ringer mode to new desired mode
                    audioManager.ringerMode = if (newMode == RingerMode.VIBRATE) {
                        AudioManager.RINGER_MODE_VIBRATE
                    } else {
                        AudioManager.RINGER_MODE_NORMAL
                    }
                    // Old setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL) is implicitly removed
                } else {
                    // Other transitions (e.g., Normal to Vibrate, Vibrate to Normal) on Android 15+
                    audioManager.ringerMode = when (newMode) {
                        RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
                        RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                        RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT // Should be handled by AZR logic, but as fallback
                    }
                }
            } else {
                // Pre-Android 15 Logic (existing behavior)
                if (newMode == RingerMode.SILENT) {
                    // DND permission already checked and handled above for this path
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                    Thread.sleep(100) // Retain sleep for older OS if it was deemed necessary
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                } else {
                    // When switching from silent to normal/vibrate on pre-Android 15
                    if (currentMode == RingerMode.SILENT) {
                        // DND permission already checked and handled above for this path
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                        Thread.sleep(100) // Retain sleep
                    }
                    // Set to normal first (if not already normal and not going to vibrate)
                    if (newMode != RingerMode.VIBRATE) {
                         audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    }

                    if (newMode == RingerMode.VIBRATE) {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    }
                }
            }

            // UI Feedback: Call params.onSettingChanged(true/false) and show toast
            // Adding a small delay to allow system to process the change before querying mode
            Thread.sleep(200) // This delay might be platform/device dependent
            val updatedMode = getCurrentRingerMode(params.context)
            val success = updatedMode == newMode

            println("Debug - Mode after change attempt: $updatedMode, Expected: $newMode, Success: $success")

            if (success) {
                val modeName = when (newMode) {
                    RingerMode.NORMAL -> "Sound mode"
                    RingerMode.SILENT -> "Silent mode"
                    RingerMode.VIBRATE -> "Vibrate mode"
                }
                SettingsUtils.showToast(params.context, modeName, true)
                params.onSettingChanged(true)
            } else {
                val currentActualModeName = when(updatedMode) { // Use updatedMode for current state
                    RingerMode.NORMAL -> "Sound"
                    RingerMode.SILENT -> "Silent"
                    RingerMode.VIBRATE -> "Vibrate"
                }
                Toast.makeText(
                    params.context,
                    "Failed to set to ${newMode.name.lowercase()}. Current mode: $currentActualModeName",
                    Toast.LENGTH_LONG
                ).show()
                params.onSettingChanged(false)
            }

        } catch (e: SecurityException) { // Catch for DND-related operations primarily
            println("SecurityException in setRingerMode: ${e.message}")
            Toast.makeText(
                params.context,
                "Operation failed. Do Not Disturb permission might be required. Please check settings.",
                Toast.LENGTH_LONG
            ).show()
            // It's good practice to open DND settings if a SecurityException related to DND occurs
            SettingsUtils.openDndPermissionSettings(params.context)
            params.onSettingChanged(false)
            // No explicit return here, as it's the end of the try-catch. Control flow will exit.
        } catch (e: Exception) { // Catch any other unexpected errors
            println("Generic Exception in setRingerMode: ${e.message}")
            Toast.makeText(
                params.context,
                "Error changing ringer mode: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            params.onSettingChanged(false)
        }
    }
}
