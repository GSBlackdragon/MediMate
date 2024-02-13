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
import com.google.mlkit.vision.common.InputImage
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ScanLoading : AppCompatActivity() {
    private lateinit var binding: LoaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = LoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textLoading.text = getString(R.string.chargement_scan)

        val imageUri = intent.getParcelableExtra<Uri>("capturedImageUri")
        Log.d("Nathan", "Coucou")
        try {

            val image = InputImage.fromFilePath(this@ScanLoading, imageUri!!)

            val ocr = OCR(SingletonDatabase.getDatabase(this@ScanLoading))
            ocr.recognize(image)
            val docList = ocr.getDoctorInfo()
            val medList = ocr.getMedicineInfo()

            startActivity(
                Intent(this@ScanLoading, ChooseMedicamentActivity::class.java)
                    .putExtra(
                        "medicamentFound",
                        medList as ArrayList<OCR.MedicationInfo>
                    )
            )
            finish()

        } catch (e: Exception) {
            Log.d("Image Error", e.toString())
            Toast.makeText(this, getString(R.string.erreur_prise_photo), Toast.LENGTH_SHORT).show()
            finish()
        }
            /*
            CoroutineScope(Dispatchers.IO).launch {

                val imageToScan = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))

                if (imageToScan != null) {
                    val text = getTextFromBitmap(imageToScan)
                    Log.d("ScanLoading", "Texte extrait: $text")
                    val ocr = OCR(SingletonDatabase.getDatabase(this@ScanLoading))
                    val medList = ocr.extractMedicationInfo(text)

                    withContext(Dispatchers.Main) {
                        startActivity(
                            Intent(this@ScanLoading, ChooseMedicamentActivity::class.java)
                                .putExtra(
                                    "medicamentFound",
                                    medList as ArrayList<OCR.MedicationInfo>
                                )
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
             */
    }



    private fun getTextFromBitmap(bitmap: Bitmap): String {
        val tess = TessBaseAPI()
        tess.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO

        val datapath = File(this.filesDir, "tesseract")
        val tessdataDirectory = File(datapath, "tessdata")

        try {
            datapath.mkdirs()
            tessdataDirectory.mkdirs()

            val trainedDataFile = File(tessdataDirectory, "fra.traineddata")

            if (!trainedDataFile.exists()) {
                assets.open("tessdata/fra.traineddata").use { inputStream ->
                    FileOutputStream(trainedDataFile).use { outStream ->
                        val buffer = ByteArray(5000)
                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outStream.write(buffer, 0, read)
                        }
                    }
                }
            }

            if (!tess.init(datapath.absolutePath, "fra")) {
                Log.e("Tesseract", "init failed")
            } else {
                tess.setImage(bitmap)
                return tess.utF8Text

            }
        } catch (e: Exception) {
            Log.e("Tesseract", "Error: ${e.message}")
        } finally {
            tess.end()
        }
        return ""
    }
}

