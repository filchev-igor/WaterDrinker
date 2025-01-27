package com.example.waterdrinker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

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

        val notificationsActivityButton: ImageView = findViewById(R.id.imageView)
        notificationsActivityButton.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        val settingsButton: Button = findViewById(R.id.button2)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val drinkButton: Button = findViewById(R.id.button10)
        drinkButton.setOnClickListener {
            val intent = Intent(this, WaterPourActivity::class.java)
            waterPourLauncher.launch(intent) // Launch WaterPourActivity
        }
    }

    override fun onResume() {
        super.onResume()
        updateDisplayedAmounts() // Reload data every time the activity is resumed
    }

    // Register for activity result to handle WaterPourActivity
    private val waterPourLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val addedAmount = result.data?.getIntExtra("WATER_AMOUNT", 0) ?: 0
                if (addedAmount > 0) {
                    lifecycleScope.launch {
                        val currentTodayAmount = DataStoreManager.readValue(this@MainActivity, todayAmountKey, 0)
                        DataStoreManager.saveValue(this@MainActivity, todayAmountKey, currentTodayAmount + addedAmount)
                        updateDisplayedAmounts()
                    }
                }
            }
        }

    private fun resetAtMidnight() {
        val currentDateKey = intPreferencesKey("current_date") // Key for storing the current date (YYYYMMDD format)

        lifecycleScope.launch {
            // Get the stored date
            val storedDate = DataStoreManager.readValue(this@MainActivity, currentDateKey, 0)

            // Get today's date in YYYYMMDD format
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.YEAR) * 10000 +
                    (calendar.get(Calendar.MONTH) + 1) * 100 +
                    calendar.get(Calendar.DAY_OF_MONTH)

            // If the stored date is not today, reset today's amount
            if (storedDate != today) {
                val todayAmount = DataStoreManager.readValue(this@MainActivity, todayAmountKey, 0)

                // Save today's amount to yesterday and reset today's amount
                DataStoreManager.saveValue(this@MainActivity, yesterdayAmountKey, todayAmount)
                DataStoreManager.saveValue(this@MainActivity, todayAmountKey, 0)

                // Update the stored date to today
                DataStoreManager.saveValue(this@MainActivity, currentDateKey, today)
            }
        }
    }


    private fun updateDisplayedAmounts() {
        val yesterdayTextView: TextView = findViewById(R.id.textView2)
        val todayTextView: TextView = findViewById(R.id.textView3)
        val goalTextView: TextView = findViewById(R.id.textView7)

        lifecycleScope.launch {
            val yesterdayAmount = DataStoreManager.readValue(this@MainActivity, yesterdayAmountKey, 0)
            val todayAmount = DataStoreManager.readValue(this@MainActivity, todayAmountKey, 0)
            val goal = DataStoreManager.readValue(this@MainActivity, waterGoalKey, 300)

            yesterdayTextView.text = "$yesterdayAmount ml"
            todayTextView.text = "$todayAmount ml"
            goalTextView.text = "$goal ml"
        }
    }
}
