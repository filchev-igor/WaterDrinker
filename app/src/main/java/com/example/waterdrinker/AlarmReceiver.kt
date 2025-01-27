package com.example.waterdrinker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Handle the alarm here
        Log.d("AlarmReceiver", "Alarm fired!")
        // You can show a notification or perform any other action
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_SHORT).show()
    }
}