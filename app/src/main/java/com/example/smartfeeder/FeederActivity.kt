package com.example.smartfeeder

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.smartfeeder.databinding.ActivityFeederBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class FeederActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeederBinding
    private val client = OkHttpClient()
    private var ready = false
    private var datr = ""
    private var ip = ""
    private var port = ""

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeder)
        datr = intent.getStringExtra("datr").toString()
        ip = intent.getStringExtra("ip").toString()
        port = intent.getStringExtra("port").toString()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_feeder)
        binding.lora = Lora("-.---", "-.---", "", "01/01/1970 00:00")

        GlobalScope.launch(Dispatchers.IO) {
            getData()
        }

        val buttonDispense = findViewById<Button>(R.id.dispense)
        val buttonApply = findViewById<Button>(R.id.apply)

        buttonDispense.setOnClickListener {
            if (!ready) {
                Toast.makeText(
                    baseContext, baseContext.getString(R.string.data_not_ready), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // TODO: action on ready
        }
        buttonApply.setOnClickListener {
            if (!ready) {
                Toast.makeText(
                    baseContext, baseContext.getString(R.string.data_not_ready), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // TODO: action on ready
        }
    }

    private fun getData() {
        val formBody = FormBody.Builder().add("datr", datr).build()
        val request = Request.Builder().url("http://$ip:$port/status").post(formBody).build()
        Log.d("d/response", "Connecting to: http://$ip:$port/status")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, applicationContext.getString(
                        R.string.no_connection_to_server), Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("d/response", response.body.toString())
            }
        })
    }
}