package com.bl4ckswordsman.cerberustiles.util

import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils

/**
 * Manages Do Not Disturb functionality using direct interruption filter approach.
 * This approach avoids ConditionProvider requirements and works on all supported versions.
 */
object AutomaticZenManager {
    
    /**
     * Checks if we can manage DND rules (requires notification policy access).
     */
    fun canManageDndRules(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }
    
    /**
     * Activates silent mode using direct interruption filter for better compatibility.
     * AutomaticZenRule approach has limitations with manual control.
     */
    fun activateSilentMode(context: Context): Boolean {
        if (!canManageDndRules(context)) {
            Toast.makeText(
                context,
                "Silent mode requires Do Not Disturb permission. Redirecting to settings...",
                Toast.LENGTH_SHORT
            ).show()
            SettingsUtils.openDndPermissionSettings(context)
            return false
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        return try {
            // Use direct interruption filter approach instead of AutomaticZenRule
            // This is more reliable for manual control
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            true
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Failed to activate silent mode",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
    
    /**
     * Deactivates silent mode by restoring normal interruption filter.
     */
    fun deactivateSilentMode(context: Context): Boolean {
        if (!canManageDndRules(context)) {
            return false
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        return try {
            // Restore normal interruption filter
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            true
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Failed to deactivate silent mode",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
    
    /**
     * Checks if silent mode is currently active via interruption filter.
     */
    fun isSilentModeActive(context: Context): Boolean {
        if (!canManageDndRules(context)) {
            return false
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        return try {
            notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
        } catch (_: Exception) {
            false
        }
    }
}
