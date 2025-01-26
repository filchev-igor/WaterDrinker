package com.example.waterdrinker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

private val android.content.Context.dataStore by preferencesDataStore("water_tracker")

class MainActivity : AppCompatActivity() {

    private val yesterdayAmountKey = intPreferencesKey("yesterday_amount")
    private val todayAmountKey = intPreferencesKey("today_amount")
    private val waterGoalKey = intPreferencesKey("water_goal")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        resetAtMidnight()
        updateDisplayedAmounts()

        val settingsButton: Button = findViewById(R.id.button2)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val drinkButton: Button = findViewById(R.id.button10)
        drinkButton.setOnClickListener {
            val intent = Intent(this, WaterPourActivity::class.java)
            waterPourLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDisplayedAmounts() // Reload data every time the activity is resumed
    }

    private fun resetAtMidnight() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val midnight = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (now >= midnight) {
            lifecycleScope.launch {
                val todayAmount = readFromDataStore(todayAmountKey).first()
                writeToDataStore(yesterdayAmountKey, todayAmount)
                writeToDataStore(todayAmountKey, 0)
            }
        }
    }

    private suspend fun writeToDataStore(key: Preferences.Key<Int>, value: Int) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    private fun readFromDataStore(key: Preferences.Key<Int>) = dataStore.data.map { preferences -> preferences[key] ?: 0 }

    private fun updateDisplayedAmounts() {
        val yesterdayTextView: TextView = findViewById(R.id.textView2)
        val todayTextView: TextView = findViewById(R.id.textView3)
        val waterConsumptionGoalAmountTextView: TextView = findViewById(R.id.textView7)

        lifecycleScope.launch {
            val yesterdayAmount = readFromDataStore(yesterdayAmountKey).first()
            val todayAmount = readFromDataStore(todayAmountKey).first()
            val waterConsumptionGoalAmount = readFromDataStore(waterGoalKey).first()

            yesterdayTextView.text = "$yesterdayAmount ml"
            todayTextView.text = "$todayAmount ml"
            waterConsumptionGoalAmountTextView.text = "$waterConsumptionGoalAmount ml"
        }
    }

    private val waterPourLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val addedAmount = result.data?.getIntExtra("WATER_AMOUNT", 0) ?: 0

                lifecycleScope.launch {
                    val currentTodayAmount = readFromDataStore(todayAmountKey).first()
                    writeToDataStore(todayAmountKey, currentTodayAmount + addedAmount)
                    updateDisplayedAmounts()
                }
            }
        }
}
