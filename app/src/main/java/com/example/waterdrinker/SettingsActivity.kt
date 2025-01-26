package com.example.waterdrinker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private val waterGoalKey = intPreferencesKey("water_goal")
    private var currentGoal: Int = 300 // Default value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mainActivityButton: Button = findViewById(R.id.button)
        mainActivityButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val editText: EditText = findViewById(R.id.editTextNumberSigned)

        // Load and display the current water goal
        lifecycleScope.launch {
            currentGoal = DataStoreManager.readValue(this@SettingsActivity, waterGoalKey, 300)
            editText.setText(currentGoal.toString())
        }

        // Save the water goal on input change, but only if it's different
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newGoal = s.toString().toIntOrNull()
                if (newGoal != null) {
                    if (newGoal != currentGoal && newGoal in 0..4000) {
                        lifecycleScope.launch {
                            DataStoreManager.saveValue(this@SettingsActivity, waterGoalKey, newGoal)
                            currentGoal = newGoal // Update the current goal
                            Toast.makeText(this@SettingsActivity, "Goal saved: $newGoal ml", Toast.LENGTH_SHORT).show()
                        }
                    } else if (newGoal !in 0..4000) {
                        Toast.makeText(this@SettingsActivity, "Enter a value between 0 and 4000", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
