package com.example.waterdrinker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

private val android.content.Context.dataStore by preferencesDataStore("water_tracker")

class SettingsActivity : AppCompatActivity() {

    private val waterGoalKey = intPreferencesKey("water_goal")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val settingsButton: Button = findViewById(R.id.button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val editText: EditText? = findViewById(R.id.editTextNumberSigned)

        editText?.let { editTextView ->
            lifecycleScope.launch {
                try {
                    val currentGoal = readWaterGoal()
                    editTextView.setText(String.format(Locale.getDefault(), "%d", currentGoal))
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@SettingsActivity, "Failed to load water goal", Toast.LENGTH_SHORT).show()
                }
            }

            editTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val newGoal = s.toString().toIntOrNull()
                    if (newGoal != null && newGoal in 0..4000) {
                        lifecycleScope.launch {
                            saveWaterGoal(newGoal)
                            Toast.makeText(this@SettingsActivity, "Goal saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SettingsActivity, "Please enter a value between 0 and 4000", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private suspend fun saveWaterGoal(goal: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[waterGoalKey] = goal
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun readWaterGoal(): Int {
        return try {
            val preferences = dataStore.data.first()
            preferences[waterGoalKey] ?: 300 // Default value
        } catch (e: Exception) {
            e.printStackTrace()
            300 // Return default value if there's an error
        }
    }
}
