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
import java.util.Locale
import kotlin.contracts.contract
import kotlin.math.log

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

    fun getDoctorInfo(): List<String> = Regex("\\d{11,}").findAll(result.text).map{ it.value }.toSet().toList()

    data class LineFuzzyScore(
        val line: String,
        val medicine: Medicine,
        val fuzzyScore: Int
    ) {
        companion object {
            fun getLinesFuzzyScore(text: Text, medicines: List<Medicine>): List<LineFuzzyScore> {
                val fuzzy = FuzzyScore(Locale.FRANCE)
                val reg = Regex("[()\\-<>]")
                val linesScore = mutableListOf<LineFuzzyScore>()

                text.textBlocks.forEach { it.lines.forEach { line ->
                    if(line.text.length > 20) {
                        medicines.forEach { med ->
                            val medString = "${med.composition?.substance_name} ${med.name} ${med.type.weight}".replace(reg, "")
                            val score = fuzzy.fuzzyScore(
                                line.text.replace(reg, ""),
                                medString
                            )
                            if(score > 30) linesScore.add(LineFuzzyScore(line.text, med, score))
                        }
                    }
                } }
                return linesScore
            }
        }
    }
    fun getMedicineInfo(): List<MedicationInfo> {

        try {
            val medicines = db.medicineDao().getAll()
            val listFuzzyScore = LineFuzzyScore.getLinesFuzzyScore(result, medicines)

            listFuzzyScore.sortedWith(compareBy<LineFuzzyScore> {it.line}.thenByDescending { it.fuzzyScore })
                .filterIndexed { index, line -> index == 0 || line.line != listFuzzyScore[index-1].line } //Filter Not working properly
                .forEach { Log.d("Fuzzy Score", it.toString()) }

        } catch (e: Exception) {
            Log.d("Fuzzy Score", e.toString())
        }

        return listOf(MedicationInfo("","","",""))
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
