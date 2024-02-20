package com.example.mms.model

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.mms.model.medicines.Medicine

@Entity(
    tableName = "medicineStorage",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["code_cis"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["medicineId"]
)
class MedicineStorage(
    var medicineId: Long,
    var storage: Int,
    var alertValue : Int
)