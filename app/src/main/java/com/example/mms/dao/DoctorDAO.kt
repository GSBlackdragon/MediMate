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

    @Query("SELECT * FROM Doctor WHERE rpps = :rpps")
    fun getDoctorById(rpps: Long): Doctor?

    @Query("SELECT * FROM Doctor WHERE name = :name")
    fun getDoctorByName(name: String): Doctor?

    @Query("SELECT * FROM Doctor WHERE firstname = :firstname")
    fun getDoctorByFirstname(firstname: String): Doctor?

    @Query("SELECT * FROM Doctor WHERE title = :title")
    fun getDoctorByTitle(title: String): Doctor?

    @Query("SELECT * FROM Doctor WHERE speciality = :speciality")
    fun getDoctorBySpeciality(speciality: String): Doctor?

    @Query("SELECT * FROM Doctor WHERE email = :email")
    fun getDoctorByEmail(email: String): Doctor?

    @Query("SELECT COUNT(*) FROM Doctor")
    fun getNbDoctor(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(doctor: Doctor)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMany(doctors: List<Doctor>)

    @Query("DELETE FROM Doctor")
    fun deleteAll()


}