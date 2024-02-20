package com.example.mms.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mms.model.Version

@Dao
interface VersionDAO {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(cycle: Version)

    @Query("SELECT * FROM version ORDER BY versionNumber DESC LIMIT 1")
    fun getFirst(): Version?
}
