package com.example.mms.database.inApp

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mms.dao.CycleDAO
import com.example.mms.dao.DoctorDAO
import com.example.mms.dao.HourWeightDAO
import com.example.mms.dao.MedicineDAO
import com.example.mms.dao.MedicineStorageDAO
import com.example.mms.dao.OneTakeDAO
import com.example.mms.dao.SpecificDaysDAO
import com.example.mms.dao.TakesDAO
import com.example.mms.dao.TaskDAO
import com.example.mms.dao.UserDAO
import com.example.mms.dao.VersionDAO
import com.example.mms.database.converter.DateConverter
import com.example.mms.model.Cycle
import com.example.mms.model.CycleHourWeight
import com.example.mms.model.Doctor
import com.example.mms.model.HourWeight
import com.example.mms.model.OneTake
import com.example.mms.model.SpecificDaysHourWeight
import com.example.mms.model.Takes
import com.example.mms.model.Task
import com.example.mms.model.User
import com.example.mms.model.MedicineStorage
import com.example.mms.model.Version
import com.example.mms.model.medicines.Medicine

// Declaration of the database
@Database(entities = [
    User::class, Medicine::class, Task::class, Cycle::class,
    CycleHourWeight::class, HourWeight::class,
    SpecificDaysHourWeight::class, Takes::class, OneTake::class,
    MedicineStorage::class, Version::class, Doctor::class
], version = 7)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDAO
    abstract fun medicineDao(): MedicineDAO
    abstract fun taskDao(): TaskDAO
    abstract fun cycleDao(): CycleDAO
    abstract fun specificDaysDao(): SpecificDaysDAO
    abstract fun hourWeightDao(): HourWeightDAO
    abstract fun takesDao(): TakesDAO
    abstract fun oneTakeDao(): OneTakeDAO
    abstract fun medicineStorageDao(): MedicineStorageDAO
    abstract fun versionDao(): VersionDAO
    abstract fun doctorDao(): DoctorDAO
}
