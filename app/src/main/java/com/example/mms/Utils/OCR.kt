package com.example.mms.Utils

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.ui.add.ScanLoading
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.net.URI

class OCR(private val db: AppDatabase) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var result: Text

    fun recognize(image: InputImage) {
        recognizer.process(image)
            .addOnSuccessListener { }
            .addOnFailureListener { throw Exception("Recognition failed") }

    }
    fun getMedicineInfo(): List<MedicationInfo> {
        return  emptyList()
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
