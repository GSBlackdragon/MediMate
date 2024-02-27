package com.example.mms.Utils

import android.os.Parcel
import android.os.Parcelable
import androidx.core.text.isDigitsOnly
import com.example.mms.database.inApp.AppDatabase
import edu.stanford.nlp.simple.Document
import java.util.regex.Pattern
import kotlin.math.abs

class OCR(private val db: AppDatabase) {

    /**
     * Extract doctors RPPS code
     *
     * @return A list of String representing RPPS code.
     */
    fun getDoctorInfo(text: String): Array<String> = Regex("\\d{11,}").findAll(text).map{ it.value }.toSet().toTypedArray()

    /**
     * Extracts the medication information from the text.
     *
     * @param text The text to extract the medication information from.
     * @return The medication information extracted from the text.
     */
    fun extractMedicationInfo(text: String): Array<MedicationInfo> {
        val tokens = tokenText(text)
        val listMedFound = extractMedicationName(tokens)
        val dosages = extractDosage(tokens)
        val listMedDos = joinMedDos(listMedFound, dosages)
        return listMedDos.map {
            MedicationInfo(
                it.value.split(" ")[0].uppercase(),
                it.value.split(" ")[1],
                "",
                ""
            )
        }.toTypedArray()

    }

    /**
     * Transforms the text into a list of tokens.
     *
     * @param text The text to transform into a list of tokens.
     * @return The list of tokens.
     */
    private fun tokenText(text: String): List<String> {
        var text2 = text.lowercase()
        text2 = transformText(text2)
        text2 = removeSpaceBetweenNumbers(text2)
        val document = Document(text2)
        return document.sentences().flatMap { it.words() }
    }

    /**
     * Extracts all the medication names from the tokens.
     *
     * @param tokens The tokens to extract the medication name from.
     * @return The medication names extracted from the tokens, with the index of the first token of the name as key.
     */
    private fun extractMedicationName(tokens: List<String>): Map<Int, String> {
        val listMedicament = db.medicineDao().getAll().map { it.name }.filter { it.length > 2 }
        val listMedFound = mutableMapOf<Int, String>()
        for (token in tokens) {
            for (medicament in listMedicament) {
                if (levenshteinDistance(token, medicament.lowercase()) <= 2) {
                    listMedFound[tokens.indexOf(token)] = medicament.lowercase()
                }
            }
        }
        return listMedFound
    }

    /**
     * Extracts all the dosages from the tokens.
     *
     * @param tokens The tokens to extract the dosages from.
     * @return The dosages extracted from the tokens, with the index of the first token of the dosage as key.
     */
    private fun extractDosage(tokens: List<String>): Map<Int, String> {
        val listDosFound = mutableMapOf<Int, String>()
        for (i in tokens.indices) {
            if (tokens[i].contains("mg")) {
                var dosage = ""
                var j = i - 1
                while (tokens[j].isDigitsOnly()) {
                    dosage = tokens[j] + dosage
                    j--
                }
                if (dosage != "")
                    listDosFound[j + 1] = dosage + "mg"
            }
        }
        return listDosFound
    }

    /**
     * Joins the medication names and dosages.
     *
     * @param listMed The medication names, with the index of the first token of the name as key.
     * @param listDos The dosages, with the index of the first token of the dosage as key.
     * @return The medication names and dosages joined, with the index of the first token of the name as key.
     */
    private fun joinMedDos(listMed : Map<Int, String>, listDos : Map<Int, String>) : Map<Int, String> {
        val listMedDos = mutableMapOf<Int, String>()
        for (i in listDos.keys) {
            for (j in listMed.keys) {
                if (abs(i - j) <= 3) {
                    listMedDos[j] = listMed[j] + " " + listDos[i]
                }
            }
        }
        return listMedDos
    }

    /**
     * Transforms the text to make it easier to extract the medication information.
     * Replaces wrongly guessed 'o' and '0' by the right one.
     *
     * @param text The text to transform.
     * @return The transformed text.
     */
    private fun transformText(text: String) : String {
        val text2 = text.replace('o', '0')
        var res = ""
        for (i in text2.indices) {
            if (text2[i] == '0') {
                when (i) {
                    0 -> {
                        res += if (text2[1] in 'a'..'z') {
                            'o'
                        } else {
                            '0'
                        }
                    }
                    text2.length - 1 -> {
                        res += if (text2[i - 1] in 'a'..'z') {
                            'o'
                        } else {
                            '0'
                        }
                    }
                    else -> {
                        res += if ((text2[i - 1] in 'a'..'z' && text2[i + 1] in 'a'..'z') ||
                            (text2[i - 1] in 'a'..'z' && text2[i + 1] == ' ') ||
                            (text2[i - 1] == ' ' && text2[i + 1] in 'a'..'z')) {
                            'o'
                        } else {
                            '0'
                        }
                    }
                }
            } else {
                res += text2[i]
            }

        }
        return res
    }

    /**
     * Removes the spaces between numbers.
     *
     * @param text The text to remove the spaces between numbers from.
     * @return The text without spaces between numbers.
     */
    fun removeSpaceBetweenNumbers(text: String) : String {
        return text.replace(Regex("(?<=\\d) +(?=\\d)"),"")
    }


    /**
     * Computes the Levenshtein distance between two CharSequences.
     *
     * @param lhs The first CharSequence.
     * @param rhs The second CharSequence.
     * @return The Levenshtein distance between the two CharSequences.
     */
    private fun levenshteinDistance(lhs : CharSequence, rhs : CharSequence) : Int {
        if (lhs == rhs) { return 0 }
        if (lhs.isEmpty()) { return rhs.length }
        if (rhs.isEmpty()) { return lhs.length }

        val len0 = lhs.length + 1
        val len1 = rhs.length + 1

        if (abs(len0 - len1) > 3){
            return 1000
        }

        var cost = Array(len0) { it }
        var newCost = Array(len0) { 0 }

        for (i in 1 until len1) {
            newCost[0] = i

            for (j in 1 until len0) {
                val match = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = minOf(minOf(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[len0 - 1]
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
