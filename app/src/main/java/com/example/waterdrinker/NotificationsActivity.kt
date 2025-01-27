package com.example.waterdrinker

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        // Create a horizontal LinearLayout to hold the alarm text and delete button
        val alarmItemLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create a TextView to display the alarm time
        val textView = TextView(this).apply {
            text = alarmTime
            textSize = 18f
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        // Create a Button to delete the alarm
        val deleteButton = Button(this).apply {
            text = "Delete"
            setOnClickListener {
                // Show a confirmation dialog before deleting the alarm
                showDeleteConfirmationDialog(alarmTime, alarmItemLayout)
            }
        }

        // Add the TextView and Button to the horizontal LinearLayout
        alarmItemLayout.addView(textView)
        alarmItemLayout.addView(deleteButton)

        // Add the horizontal LinearLayout to the main alarms LinearLayout
        alarmsLayout.addView(alarmItemLayout)
    }

    private fun showDeleteConfirmationDialog(alarmTime: String, alarmItemLayout: LinearLayout) {
        // Create an AlertDialog to confirm deletion
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Delete Alarm")
            .setMessage("Are you sure you want to delete the alarm $alarmTime?")
            .setPositiveButton("OK") { dialog, _ ->
                // Remove the alarm from the UI
                alarmsLayout.removeView(alarmItemLayout)
                // Delete the alarm from DataStore
                lifecycleScope.launch {
                    deleteAlarm(alarmTime)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Show the dialog
        alertDialog.show()

        // Customize button colors
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ed750c")))
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(this, android.R.color.white)
        )
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(this, android.R.color.black)
        )
    }

    private suspend fun saveAlarm(alarmTime: String) {
        // Convert the alarm time to an Int (e.g., "12:30" -> 1230)
        val alarmInt = alarmTime.replace(":", "").toIntOrNull() ?: return

        // Generate a unique key for the alarm
        val key = intPreferencesKey("$ALARMS_KEY_PREFIX$alarmInt")

        // Save the alarm using DataStoreManager
        DataStoreManager.saveValue(this, key, alarmInt)

        // Schedule the alarm
        scheduleAlarm(alarmTime)
    }

    private suspend fun deleteAlarm(alarmTime: String) {
        // Convert the alarm time to an Int (e.g., "12:30" -> 1230)
        val alarmInt = alarmTime.replace(":", "").toIntOrNull() ?: return

        // Generate the key for the alarm
        val key = intPreferencesKey("$ALARMS_KEY_PREFIX$alarmInt")

        // Delete the alarm from DataStore by setting its value to -1 (or any invalid value)
        DataStoreManager.saveValue(this, key, -1)
    }

    private suspend fun loadAlarms() {
        // Retrieve all alarms from DataStore
        val alarms = mutableSetOf<String>()

        // Example: Load alarms with keys like "alarm_1230"
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

    private fun scheduleAlarm(alarmTime: String) {
        // Convert the alarm time to a Calendar instance
        val calendar = Calendar.getInstance().apply {
            val (hour, minute) = alarmTime.split(":").map { it.toInt() }
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Create an Intent for the AlarmReceiver
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmTime.hashCode(), // Use a unique request code for each alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d("NotificationsActivity", "Alarm scheduled for $alarmTime")
    }
}