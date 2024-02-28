package com.example.mms.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.mms.model.SpecificDaysHourWeight

@Dao
fun interface SpecificDaysDAO {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(specificDays: SpecificDaysHourWeight)
}
