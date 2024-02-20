package com.example.mms.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Version (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var versionNumber: Int
)
