package com.example.mms.model.medicines

import androidx.room.Entity
import kotlinx.serialization.Serializable
import java.text.Normalizer


@Serializable
@Entity
data class GroupOfMedicines(
    private val medicines: List<MedicinesByName>,
) {
    fun getAllMedicines(): List<Medicine> {
        val allMedicines = mutableListOf<Medicine>()

        for (medicines in this.medicines) {
            allMedicines.addAll(medicines.getAllMedicines())
        }

        return allMedicines
    }

    fun getOneMedicineByCIS(codeCis: Long): Medicine? {
        var medicine: Medicine?
        for (medicines in this.medicines) {
            medicine = medicines.getMedicineByCIS(codeCis)
            if (medicine != null) {
                return medicine
            }
        }
        return null
    }

    fun getMedicinesByName(targetName: String): List<Medicine> {
        val normalizedName = Normalizer.normalize(targetName, Normalizer.Form.NFD)

        for (medicines in this.medicines) {
            if (medicines.name == normalizedName) {
                return medicines.getAllMedicines()
            }
        }
        return mutableListOf()
    }

    fun getRandomMedicines(number: Int = 10): List<Medicine> {
        return this.getAllMedicines().shuffled().take(number)
    }

    override fun toString(): String {
        return "${this.medicines.size}"
    }
}
