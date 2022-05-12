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
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt
import java.text.DecimalFormat
import kotlin.math.round

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
            // TODO: pass dispense command, get new data
            ready = false
        }
        buttonApply.setOnClickListener {
            if (!ready) {
                Toast.makeText(
                    baseContext, baseContext.getString(R.string.data_not_ready), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // TODO: pass change to the database, get new data
            ready = false
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
                var data = "-.---\t-.---\t\t01/01/1970 00:00"
                response.body?.let {
                    data = it.string()
                }
                val result = parse(data)
                binding.lora = Lora(result[0], result[1], result[2], result[3])
                ready = true
            }
        })
    }

    private fun parse(data: String): Array<String> {
        val parts = data.split("\t")

        val format = DecimalFormat("0.#")
        val weight = DecimalFormat("0.000")

        val stock = weight.format(round(parseDouble(parts[0])) / 1000).toString()
        val plate = weight.format(round(parseDouble(parts[1])) / 1000).toString()
        val rate = format.format(parseInt(parts[2]).toDouble() / 60).toString()
        val fed = parseDateTime(parts[3])

        return arrayOf(stock, plate, rate, fed)
    }

    private fun parseDateTime(timestamp: String): String {
        val year = timestamp.subSequence(0, 4)
        val month = timestamp.subSequence(4, 6)
        val day = timestamp.subSequence(6, 8)
        val hour = timestamp.subSequence(8, 10)
        val minute = timestamp.subSequence(10, 12)
        // val second = timestamp.subSequence(12, 14)

        return "$day/$month/$year $hour:$minute"
    }
}