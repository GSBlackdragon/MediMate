package com.example.mms.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mms.model.SideInfoMedicine

@Dao
interface SideInfoMedicineDAO {

    @Query("SELECT * FROM SideInfoMedicine")
    fun getAll(): List<SideInfoMedicine>?

    @Query("SELECT * FROM SideInfoMedicine WHERE code_cis = :codeCis")
    fun getById(codeCis: String): SideInfoMedicine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(medicine: SideInfoMedicine)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMany(medicines: List<SideInfoMedicine>)

    @Delete
    fun delete(medicine: SideInfoMedicine)

    @Query("DELETE FROM SideInfoMedicine")
    fun deleteAll()

    @Query("UPDATE SideInfoMedicine SET sideInfo = :sideInfo WHERE code_cis = :codeCis")
    fun addBadInteraction(codeCis: String, sideInfo: String)
}