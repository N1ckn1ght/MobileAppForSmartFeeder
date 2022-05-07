package com.example.smartfeeder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.smartfeeder.databinding.ActivityFeederBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FeederActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeederBinding

    private var ready = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeder)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_feeder)
        binding.lora = Lora("-.---", "-.---", "")

        GlobalScope.launch(Dispatchers.IO) {
            getData()
        }

        val buttonDispense = findViewById<Button>(R.id.dispense)
        val buttonApply = findViewById<Button>(R.id.apply)

        buttonDispense.setOnClickListener() {
            Toast.makeText(baseContext, "Won\'t dispense \'til data is ready",
                Toast.LENGTH_SHORT).show()
        }
        buttonApply.setOnClickListener() {
            Toast.makeText(baseContext, "Won\'t apply \'til data is ready",
                Toast.LENGTH_SHORT).show()
        }

        val editTextRate = findViewById<EditText>(R.id.rate_second)
        editTextRate.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private fun getData() {

    }
}