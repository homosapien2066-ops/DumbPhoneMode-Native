package com.example.dumbphonemode

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var rvApps: RecyclerView
    private lateinit var btnSave: Button
    private lateinit var tvSelectionCount: TextView
    private lateinit var prefs: SharedPreferences
    private val selectedApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("DumbphonePrefs", Context.MODE_PRIVATE)
        selectedApps.addAll(prefs.getStringSet("selectedApps", emptySet()) ?: emptySet())

        tvSelectionCount = findViewById(R.id.tvSelectionCount)
        rvApps = findViewById(R.id.rvApps)
        btnSave = findViewById(R.id.btnSave)

        updateSelectionText()

        val allApps = getInstalledApps()
        val adapter = SelectableAppsAdapter(allApps, selectedApps) {
            updateSelectionText()
        }
        
        rvApps.layoutManager = LinearLayoutManager(this)
        rvApps.adapter = adapter

        btnSave.setOnClickListener {
            prefs.edit().putStringSet("selectedApps", selectedApps).apply()
            Toast.makeText(this, "Selections saved! You can now trigger Dumbphone Mode.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun updateSelectionText() {
        tvSelectionCount.text = "Select up to 5 essential apps (${selectedApps.size}/5)"
    }

    private fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        
        val apps = mutableListOf<AppInfo>()
        for (resolveInfo in resolveInfoList) {
            if (resolveInfo.activityInfo.packageName != packageName) {
                val componentName = "${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}"
                apps.add(AppInfo(
                    resolveInfo.loadLabel(packageManager).toString(),
                    componentName,
                    resolveInfo.loadIcon(packageManager)
                ))
            }
        }
        return apps.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
}

data class AppInfo(val label: String, val componentName: String, val icon: android.graphics.drawable.Drawable)

class SelectableAppsAdapter(
    private val appsList: List<AppInfo>, 
    private val selectedApps: MutableSet<String>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<SelectableAppsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivIcon)
        val label: TextView = view.findViewById(R.id.tvName)
        val checkBox: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selectable_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appsList[position]
        holder.label.text = app.label
        holder.icon.setImageDrawable(app.icon)
        
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedApps.contains(app.componentName)
        
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
        
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (selectedApps.size >= 5) {
                    holder.checkBox.isChecked = false
                    Toast.makeText(holder.itemView.context, "Maximum 5 apps allowed", Toast.LENGTH_SHORT).show()
                } else {
                    selectedApps.add(app.componentName)
                    onSelectionChanged()
                }
            } else {
                selectedApps.remove(app.componentName)
                onSelectionChanged()
            }
        }
    }

    override fun getItemCount() = appsList.size
}
