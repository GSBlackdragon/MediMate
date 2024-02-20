package com.example.mms.Utils

import android.icu.text.Normalizer2
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.model.medicines.Medicine
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.apache.commons.text.similarity.FuzzyScore
import org.apache.commons.text.similarity.HammingDistance
import org.apache.commons.text.similarity.LevenshteinDistance
import org.apache.commons.text.similarity.LongestCommonSubsequence
import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance
import java.util.Locale

class OCR(private val db: AppDatabase) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var result: Text

    private fun String.unaccent(): String {
        return Normalizer2.getNFDInstance().normalize(this).replace(Regex("\\p{Mn}"), "")
    }

    fun recognize(image: InputImage) {
        Tasks.await(
            recognizer.process(image)
            .addOnSuccessListener { result = it; Log.d("Image Recovery", "Success : image read correctly")}
            .addOnFailureListener { Log.d("Image Recovery", "Failure : image not read"); throw Exception("Recognition failed") }
        )
    }
    fun getMedicineInfo(): List<MedicationInfo> {

        val medicines = db.medicineDao().getAll()
        val detectedMedicines = mutableListOf<Medicine>()
        val jws = FuzzyScore(Locale.FRENCH)
        val reg = Regex("[()\\-<>]")

        val airomir = medicines.find { it.code_cis == 66086181L }?.let { "${it.composition?.substance_name} ${it.name} ${it.type.weight}" }
        val score = jws.fuzzyScore(
            "1/ Salbutamol AlROMIR 100MCG/DOSE AUTOHALER 200  1 Flacon".uppercase(),
            airomir!!.replace(reg, "")
        )
        Log.d("JWS Score", "${airomir.replace(reg, "")} -> $score")

        result.textBlocks.forEach { it.lines.forEach { line ->
            if(line.text.length > 10) {
                medicines.forEach { med ->
                    val medString = "${med.composition?.substance_name} ${med.name} ${med.type.weight}"
                    val score = jws.fuzzyScore(line.text.replace(reg, ""), medString.replace(reg, ""))
                    if(score > 38) {
                        detectedMedicines.add(med)
                        Log.d("JWS Score", "${line.text.replace(reg, "")} -> ${medString.replace(reg, "")} : $score")
                    }
                }
            }
        } }

        Log.d("MedicinesList", detectedMedicines.toString())
        return emptyList()
    }

    fun getDoctorInfo(): List<String> {
        return Regex("\\d{11,}").findAll(result.text).map{ it.value }.toSet().toList()
    }

    /**
     * Data class representing the medication information.
     *
     * @property name The name of the medication.
     * @property dosage The dosage of the medication.
     * @property frequency The frequency of the medication.
     * @property duration The duration of the medication.
     */
    data class MedicationInfo(
        val name: String,
        val dosage: String,
        val frequency: String,
        val duration: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(dosage)
            parcel.writeString(frequency)
            parcel.writeString(duration)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<MedicationInfo> {
            override fun createFromParcel(parcel: Parcel): MedicationInfo {
                return MedicationInfo(parcel)
            }

            override fun newArray(size: Int): Array<MedicationInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

}
