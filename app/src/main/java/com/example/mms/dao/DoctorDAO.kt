package com.example.mms.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mms.model.Doctor

@Dao
interface DoctorDAO {

    @Query("SELECT * FROM Doctor")
    fun getAll(): Doctor?

    @Query("SELECT * FROM Doctor WHERE pp = :pp")
    fun getDoctorById(pp: Long): Doctor?

    @Query("SELECT * FROM Doctor WHERE name = :name")
    fun getDoctorByName(name: String): Doctor?

    @Query("SELECT * FROM Doctor WHERE firstname = :firstname")
    fun getDoctorByFirstname(firstname: String): Doctor?

    @Query("SELECT * FROM Doctor WHERE typeDiplome = :typeDiplome")
    fun getDoctorByTypeDiplome(typeDiplome: String): Doctor?

    @Query("SELECT * FROM Doctor WHERE codeDiplome = :codeDiplome")
    fun getDoctorByCodeDiplome(codeDiplome: String): Doctor?

    @Query("SELECT * FROM Doctor WHERE libelleDiplome = :libelleDiplome")
    fun getDoctorByLibelleDiplome(libelleDiplome: String): Doctor?

    @Query("SELECT COUNT(*) FROM Doctor")
    fun getNbDoctor(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(doctor: Doctor)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMany(doctors: List<Doctor>)

    @Query("DELETE FROM Doctor")
    fun deleteAll()


}