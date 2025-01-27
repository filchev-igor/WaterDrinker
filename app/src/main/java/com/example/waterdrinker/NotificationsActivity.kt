package com.example.waterdrinker

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class NotificationsActivity : AppCompatActivity() {

    private lateinit var alarmsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        alarmsLayout = findViewById(R.id.linearLayout)
        val fab: FloatingActionButton = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            showTimePickerDialog()
        }

        loadAlarms()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val alarmTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                saveAlarm(alarmTime)
                addAlarmToLayout(alarmTime)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun addAlarmToLayout(alarmTime: String) {
        val textView = TextView(this).apply {
            text = alarmTime
            textSize = 18f
            setPadding(16, 16, 16, 16)
        }
        alarmsLayout.addView(textView)
    }

    private fun saveAlarm(alarmTime: String) {
        val sharedPreferences = getSharedPreferences("Alarms", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val alarms = sharedPreferences.getStringSet("alarmSet", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        alarms.add(alarmTime)
        editor.putStringSet("alarmSet", alarms)
        editor.apply()
    }

    private fun loadAlarms() {
        val sharedPreferences = getSharedPreferences("Alarms", MODE_PRIVATE)
        val alarms = sharedPreferences.getStringSet("alarmSet", mutableSetOf()) ?: mutableSetOf()
        for (alarm in alarms) {
            addAlarmToLayout(alarm)
        }
    }
}