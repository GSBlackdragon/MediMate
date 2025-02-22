package com.example.mms.ui.add

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mms.R
import com.example.mms.Utils.OCR
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.LoaderBinding
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanLoading : AppCompatActivity() {
    private lateinit var binding: LoaderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = LoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textLoading.text = getString(R.string.chargement_scan)

        val imageUri = intent.getParcelableExtra<Uri>("capturedImageUri")
        if (imageUri != null) {
            CoroutineScope(Dispatchers.IO).launch {

                val imageToScan = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))

                if (imageToScan != null) {
                    val text = getTextFromBitmap(imageToScan)
                    Log.d("ImageToText", text)
                    val ocr = OCR(SingletonDatabase.getDatabase(this@ScanLoading))
                    val medList = ocr.extractMedicationInfo(text)
                    val doctorList = ocr.getDoctorInfo(text)
                    withContext(Dispatchers.Main) {
                        startActivity(
                            Intent(this@ScanLoading, ChooseMedicamentActivity::class.java)
                                .putExtra("medicamentFound", medList)
                                .putExtra("doctorFound", doctorList)
                        )
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ScanLoading, getString(R.string.erreur_prise_photo), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.erreur_prise_photo), Toast.LENGTH_SHORT).show()
            finish()
        }
    }



    private fun getTextFromBitmap(bitmap: Bitmap): String {
        return Tasks.await(
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { Log.d("ImageToText", "Image read correctly") }
                .addOnFailureListener { Log.e("ImageToText", it.toString()) }
        ).text
    }
}

