package com.example.waterdrinker

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationsActivity : AppCompatActivity() {

    private lateinit var alarmsLayout: LinearLayout

    // Key prefix for storing alarms in DataStore
    private val ALARMS_KEY_PREFIX = "alarm_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the LinearLayout for alarms
        alarmsLayout = findViewById(R.id.linearLayout)

        // Set up the FloatingActionButton
        val fab: FloatingActionButton = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            showTimePickerDialog()
        }

        // Load saved alarms (if any)
        lifecycleScope.launch {
            loadAlarms()
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Show a TimePickerDialog to select the alarm time
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val alarmTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                lifecycleScope.launch {
                    saveAlarm(alarmTime) // Save the alarm
                }
                addAlarmToLayout(alarmTime) // Add the alarm to the UI
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun addAlarmToLayout(alarmTime: String) {
        // Create a TextView to display the alarm time
        val textView = TextView(this).apply {
            text = alarmTime
            textSize = 18f
            setPadding(16, 16, 16, 16)
        }
        // Add the TextView to the LinearLayout
        alarmsLayout.addView(textView)
    }

    private suspend fun saveAlarm(alarmTime: String) {
        // Convert the alarm time to an Int (e.g., "12:30" -> 1230)
        val alarmInt = alarmTime.replace(":", "").toIntOrNull() ?: return

        // Generate a unique key for the alarm
        val key = intPreferencesKey("$ALARMS_KEY_PREFIX$alarmInt")

        // Save the alarm using DataStoreManager
        DataStoreManager.saveValue(this, key, alarmInt)
    }

    private suspend fun loadAlarms() {
        // Retrieve all alarms from DataStore
        // Note: This approach assumes you know the key format for alarms
        // In a real app, you might need to store a list of keys separately
        val alarms = mutableSetOf<String>()

        // Example: Load alarms with keys like "alarm_1230"
        // This is a simplified approach; you might need a more robust solution
        for (i in 0..23) {
            for (j in 0..59) {
                val alarmTime = String.format("%02d%02d", i, j)
                val key = intPreferencesKey("$ALARMS_KEY_PREFIX$alarmTime")
                val alarmInt = DataStoreManager.readValue(this, key, -1)
                if (alarmInt != -1) {
                    alarms.add("${alarmTime.substring(0, 2)}:${alarmTime.substring(2)}")
                }
            }
        }

        // Add each alarm to the UI
        for (alarm in alarms) {
            addAlarmToLayout(alarm)
        }
    }
}