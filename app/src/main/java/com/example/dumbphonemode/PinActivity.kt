package com.example.dumbphonemode

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PinActivity : AppCompatActivity() {

    private lateinit var tvClock: TextView
    private lateinit var llAppContainer: LinearLayout

    private val unpinReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.dumbphonemode.UNPIN") {
                stopLockTask()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        tvClock = findViewById(R.id.tvClock)
        llAppContainer = findViewById(R.id.llAppContainer)

        setupDynamicButtons()

        ContextCompat.registerReceiver(this, unpinReceiver, IntentFilter("com.example.dumbphonemode.UNPIN"), ContextCompat.RECEIVER_EXPORTED)

        startLockTask()
    }

    override fun onResume() {
        super.onResume()
        updateClock()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(unpinReceiver)
    }

    private fun updateClock() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvClock.text = sdf.format(Date())
    }

    private fun setupDynamicButtons() {
        val prefs = getSharedPreferences("DumbphonePrefs", Context.MODE_PRIVATE)
        val selectedApps = prefs.getStringSet("selectedApps", emptySet()) ?: emptySet()
        
        if (selectedApps.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "No apps configured.\nOpen DumbphoneMode in your app drawer to set up."
                setTextColor(android.graphics.Color.WHITE)
                gravity = Gravity.CENTER
                textSize = 16f
            }
            llAppContainer.addView(tvEmpty)
            return
        }

        val scale = resources.displayMetrics.density
        val width = (200 * scale + 0.5f).toInt()
        val height = (64 * scale + 0.5f).toInt()
        val margin = (16 * scale + 0.5f).toInt()

        for (componentString in selectedApps) {
            val parts = componentString.split("/")
            if (parts.size != 2) continue
            val pkg = parts[0]
            val cls = parts[1]

            val intent = Intent()
            intent.setComponent(ComponentName(pkg, cls))
            val resolveInfo = packageManager.resolveActivity(intent, 0)
            
            val label = resolveInfo?.loadLabel(packageManager)?.toString() ?: pkg
            
            val button = Button(this).apply {
                text = label
                textSize = 20f
                setBackgroundColor(android.graphics.Color.parseColor("#222222"))
                setTextColor(android.graphics.Color.WHITE)
                
                val params = LinearLayout.LayoutParams(width, height)
                params.setMargins(0, 0, 0, margin)
                layoutParams = params
                
                setOnClickListener {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            llAppContainer.addView(button)
        }
    }
}
