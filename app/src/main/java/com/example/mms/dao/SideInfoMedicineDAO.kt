package com.example.mms.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mms.model.SideInfoMedicine

@Dao
interface SideInfoMedicineDAO {

    @Query("SELECT * FROM SideInfoMedicine")
    fun getAll(): List<SideInfoMedicine>?

}