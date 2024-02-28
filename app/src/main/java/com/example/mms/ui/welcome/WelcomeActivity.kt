package com.example.mms.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mms.databinding.WelcomeActivityBinding
import com.example.mms.ui.createAccount.CreateAccountActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: WelcomeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = WelcomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get buttons
        val cbCGU = binding.checkBoxCGU
        val cbPC = binding.checkBoxPC
        val btnCA = binding.buttonCreateAccount
        btnCA.isEnabled = false


        cbCGU.setOnClickListener {
            btnCA.isEnabled = cbCGU.isChecked && cbPC.isChecked

            // change button style
            if (btnCA.isEnabled) {
                btnCA.setBackgroundResource(com.example.mms.R.drawable.button_style_3)
                btnCA.setTextColor(resources.getColor(com.example.mms.R.color.clickable_blue))
            } else {
                btnCA.setBackgroundResource(com.example.mms.R.drawable.button_style_3_disable)
                btnCA.setTextColor(resources.getColor(com.example.mms.R.color.clickable_blue_disable))
            }
        }

        cbPC.setOnClickListener {
            btnCA.isEnabled = cbCGU.isChecked && cbPC.isChecked

            // change button style
            if (btnCA.isEnabled) {
                btnCA.setBackgroundResource(com.example.mms.R.drawable.button_style_3)
                btnCA.setTextColor(resources.getColor(com.example.mms.R.color.clickable_blue))
            } else {
                btnCA.setBackgroundResource(com.example.mms.R.drawable.button_style_3_disable)
                btnCA.setTextColor(resources.getColor(com.example.mms.R.color.clickable_blue_disable))
            }
        }

        btnCA.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        // get text views
        val textCGU = binding.textCGU
        val textPC = binding.textPC

        // set on click listeners
        textCGU.setOnClickListener {
            startActivity(Intent(this, ActivityCGU::class.java))
        }

        textPC.setOnClickListener {
            startActivity(Intent(this, ActivityPC::class.java))
        }
    }
}
