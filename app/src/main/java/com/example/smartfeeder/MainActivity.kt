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

        val etIdClient = findViewById<EditText>(R.id.id_client)
        val etIpServer = findViewById<EditText>(R.id.ip_server)
        val etPort = findViewById<EditText>(R.id.port)
        val button = findViewById<Button>(R.id.toFeeder)

        button.setOnClickListener {
            val intent = Intent(this, FeederActivity::class.java)
            intent.putExtra("idClient", etIdClient.text.toString().toInt())
            intent.putExtra("ipServer", etIpServer.text.toString())
            intent.putExtra("port", etPort.text.toString())
            startActivity(intent)
        }
    }
}