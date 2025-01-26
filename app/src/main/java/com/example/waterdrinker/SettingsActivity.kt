package com.example.waterdrinker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

// DataStore instance
private val android.content.Context.dataStore by preferencesDataStore("water_tracker")

class SettingsActivity : AppCompatActivity() {

    // DataStore keys
    private val waterGoalKey = intPreferencesKey("water_goal") // Key for storing the water goal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Handle window insets (for edge-to-edge design)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editText = findViewById<EditText>(R.id.editTextNumberSigned)
        val saveButton = findViewById<Button>(R.id.button)

        // Load current goal and set it in EditText
        lifecycleScope.launch {
            val currentGoal = readWaterGoal()

            editText.setText(String.format(Locale.getDefault(), "%d", currentGoal))

        }

        // Save the water goal when the save button is clicked
        saveButton.setOnClickListener {
            val newGoal = editText.text.toString().toIntOrNull()
            if (newGoal != null) {
                lifecycleScope.launch {
                    saveWaterGoal(newGoal) // Save the new goal to DataStore
                    Toast.makeText(this@SettingsActivity, "Goal saved successfully!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to save the water goal to DataStore
    private suspend fun saveWaterGoal(goal: Int) {
        dataStore.edit { preferences ->
            preferences[waterGoalKey] = goal
        }
    }

    // Function to read the current water goal from DataStore
    private suspend fun readWaterGoal(): Int {
        val preferences = dataStore.data.first() // Retrieve the current preferences snapshot
        return preferences[waterGoalKey] ?: 1000 // Default value is 1000 ml
    }
}
