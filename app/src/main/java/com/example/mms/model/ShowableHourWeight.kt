package com.example.mms.model

import com.example.mms.model.medicines.MType

class ShowableHourWeight (
    val medicineName: String,
    val medicineType: MType,
    val task: Task,
    val hourWeight: HourWeight,
    val medicineStorage: MedicineStorage?
) {
    /**
     * If the storage is enough to take the medicine
     *
     * @return true if the storage is enough, false otherwise
     */
    fun hasEnoughStock(): Boolean {
        if (this.medicineStorage == null) {
            return true
        }

        return this.medicineStorage.storage >= this.hourWeight.weight
    }
}
