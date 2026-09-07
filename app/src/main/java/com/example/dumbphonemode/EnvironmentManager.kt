package com.example.dumbphonemode

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log

class EnvironmentManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("DumbphonePrefs", Context.MODE_PRIVATE)
    
    fun isDumbphoneModeActive(): Boolean {
        return prefs.getBoolean("isActive", false)
    }

    fun toggleMode(): Boolean {
        val newState = !isDumbphoneModeActive()
        prefs.edit().putBoolean("isActive", newState).apply()
        
        applyState(newState)
        return newState
    }
    
    fun applyState(isActive: Boolean) {
        setDnd(isActive)
    }

    private fun setDnd(enabled: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            if (enabled) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        } else {
            Log.e("EnvironmentManager", "DND access not granted")
        }
    }
}
