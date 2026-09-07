package com.example.dumbphonemode

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class TriggerService : AccessibilityService() {
    private var volumeUpTime: Long = 0
    private var volumeDownTime: Long = 0
    private val CHORD_WINDOW_MS = 300L
    
    private lateinit var environmentManager: EnvironmentManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        environmentManager = EnvironmentManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used
    }

    override fun onInterrupt() {
        // Not used
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        val time = event.eventTime

        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                volumeUpTime = time
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                volumeDownTime = time
            }

            if (volumeUpTime > 0 && volumeDownTime > 0) {
                val diff = Math.abs(volumeUpTime - volumeDownTime)
                if (diff <= CHORD_WINDOW_MS) {
                    volumeUpTime = 0
                    volumeDownTime = 0
                    
                    val isActive = environmentManager.toggleMode()
                    
                    if (isActive) {
                        // Launch PinActivity
                        val intent = Intent(this, PinActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        // Broadcast to PinActivity to stopLockTask and finish
                        val intent = Intent("com.example.dumbphonemode.UNPIN")
                        sendBroadcast(intent)
                    }
                    
                    return true // Consume the event to prevent actual volume change
                }
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                volumeUpTime = 0
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                volumeDownTime = 0
            }
        }
        return super.onKeyEvent(event)
    }
}
