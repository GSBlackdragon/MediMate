package com.example.mms.model

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.mms.model.medicines.Medicine

@Entity(
    tableName = "sideInfoMedicine",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["code_cis"],
            childColumns = ["code_cis"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["code_cis"]
)
class SideInfoMedicine (
        var code_cis : String,
        var warning : String,
        var allergie : String,
        var content : String,
        var sideInfo : String
)