package com.example.smartfeeder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etDatr = findViewById<EditText>(R.id.datr)
        val etIp = findViewById<EditText>(R.id.ip)
        val etPort = findViewById<EditText>(R.id.port)
        val button = findViewById<Button>(R.id.toFeeder)

        button.setOnClickListener {
            val intent = Intent(this, FeederActivity::class.java)
            intent.putExtra("datr", etDatr.text.toString())
            intent.putExtra("ip", etIp.text.toString())
            intent.putExtra("port", etPort.text.toString())
            startActivity(intent)
        }
    }
}