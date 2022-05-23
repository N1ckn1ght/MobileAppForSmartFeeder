package com.example.smartfeeder

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
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
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt
import java.text.DecimalFormat
import kotlin.math.round

class FeederActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeederBinding
    private lateinit var buttonDispense: Button
    private lateinit var buttonApply: Button
    private val client = OkHttpClient()
    private var ready = false
    private var idClient = 0
    private var ipServer = ""
    private var port = ""

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeder)
        idClient = intent.getIntExtra("idClient", 0)
        ipServer = intent.getStringExtra("ipServer").toString()
        port = intent.getStringExtra("port").toString()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_feeder)
        binding.lora = Lora("-.---", "-.---", "", "01/01/1970 00:00")

        GlobalScope.launch(Dispatchers.IO) {
            getData()
        }

        buttonDispense = findViewById(R.id.dispense)
        buttonApply = findViewById(R.id.apply)

        buttonDispense.setOnClickListener {
            if (!ready) {
                Toast.makeText(
                    baseContext, baseContext.getString(R.string.data_not_ready), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            setReady(false)
            GlobalScope.launch(Dispatchers.IO) {
                sendData("dispense", mutableListOf())
            }
        }
        buttonApply.setOnClickListener {
            if (!ready) {
                Toast.makeText(
                    baseContext, baseContext.getString(R.string.data_not_ready), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val etRate = findViewById<EditText>(R.id.rate_second)
            setReady(false)
            GlobalScope.launch(Dispatchers.IO) {
                sendData("rate", mutableListOf(etRate.text.toString()))
            }
        }
    }

    private fun getData() {
        val formBody = FormBody.Builder().add("id", idClient.toString()).build()
        val request = Request.Builder().url("http://$ipServer:$port/get").post(formBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, applicationContext.getString(
                        R.string.no_connection_to_server), Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                var data = "-.---\t-.---\t\t01/01/1970 00:00"
                response.body?.let {
                    data = it.string()
                }
                val result = parse(data)
                binding.lora = Lora(result[0], result[1], result[2], result[3])
                runOnUiThread {
                    setReady(true)
                }
            }
        })
    }

    private fun sendData(name: String, argv: MutableList<String>) {
        val form = FormBody.Builder().add("id", idClient.toString())
        if (name == "dispense") {
            form.add("dispense", "1")
        }
        if (name == "rate") {
            form.add("rate", argv[0])
        }
        val formBody = form.build()
        val request = Request.Builder().url("http://$ipServer:$port/set").post(formBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, applicationContext.getString(
                        R.string.no_connection_to_server), Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                var result = ""
                response.body?.let {
                    result = it.string()
                }
                if (result == "error") {
                    runOnUiThread {
                        Toast.makeText(applicationContext, applicationContext
                            .getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, applicationContext
                            .getString(R.string.please_wait), Toast.LENGTH_LONG).show()
                    }
                    @Suppress("DEPRECATION")
                    Handler(Looper.getMainLooper()).postDelayed({
                        getData()
                    }, 10000)
                }
            }
        })
    }

    private fun parse(data: String): Array<String> {
        val parts = data.split("\t")

        val weight = DecimalFormat("0.000")

        val stock = weight.format(round(parseDouble(parts[0])) / 1000).toString()
        val plate = weight.format(round(parseDouble(parts[1])) / 1000).toString()
        val rate = parseInt(parts[2]).toString()
        val fed = parseDateTime(parts[3])

        return arrayOf(stock, plate, rate, fed)
    }

    private fun parseDateTime(timestamp: String): String {
        val year = timestamp.subSequence(0, 4)
        val month = timestamp.subSequence(4, 6)
        val day = timestamp.subSequence(6, 8)
        val hour = timestamp.subSequence(8, 10)
        val minute = timestamp.subSequence(10, 12)

        return "$day/$month/$year $hour:$minute"
    }

    private fun setReady(ready_: Boolean) {
        if (ready_) {
            buttonApply.isEnabled = true
            buttonDispense.isEnabled = true
        } else {
            buttonApply.isEnabled = false
            buttonDispense.isEnabled = false
        }
        ready = ready_
    }
}