package com.example.mms.ui.add

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mms.databinding.ActivityAddMedicamentBinding

class AddMedicamentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicamentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val medicineName = intent.getStringExtra("medicineName") ?: ""
        val fromOcr = intent.getBooleanExtra("fromOcr", false)
        val viewModel = ViewModelProvider(this)[SharedAMViewModel::class.java]
        viewModel.setMedicineName(medicineName)
        viewModel.setFromOCR(fromOcr)
        supportActionBar?.hide()
        binding = ActivityAddMedicamentBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


}