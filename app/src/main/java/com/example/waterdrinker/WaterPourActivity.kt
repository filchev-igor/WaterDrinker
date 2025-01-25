package com.example.waterdrinker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WaterPourActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_water_pour)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button100ml: Button = findViewById(R.id.button3)
        val button150ml: Button = findViewById(R.id.button4)
        val button200ml: Button = findViewById(R.id.button5)
        val button250ml: Button = findViewById(R.id.button6)
        val button300ml: Button = findViewById(R.id.button7)
        val button350ml: Button = findViewById(R.id.button8)

        button100ml.setOnClickListener { sendResultAndFinish(100) }
        button150ml.setOnClickListener { sendResultAndFinish(150) }
        button200ml.setOnClickListener { sendResultAndFinish(200) }
        button250ml.setOnClickListener { sendResultAndFinish(250) }
        button300ml.setOnClickListener { sendResultAndFinish(300) }
        button350ml.setOnClickListener { sendResultAndFinish(350) }
    }

    private fun sendResultAndFinish(amount: Int) {
        val intent = Intent().apply { putExtra("WATER_AMOUNT", amount) }
        setResult(RESULT_OK, intent)
        finish()
    }
}
