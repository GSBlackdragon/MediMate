package com.example.mms.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mms.model.Cycle
import com.example.mms.model.SpecificDaysHourWeight
import com.example.mms.model.Takes
import com.example.mms.model.Task

@Dao
interface TaskDAO {

    @Query("SELECT * FROM task")
    fun getAll(): MutableList<Task>

    @Query("SELECT * FROM task WHERE userId = :userId")
    fun getUserTasks(userId: String): MutableList<Task>

    @Query("SELECT * FROM task WHERE id = :taskId")
    fun getTask(taskId: Long): Task

    @Query("SELECT * FROM cycle WHERE taskId = :taskId")
    fun getTaskCycle(taskId: Long): Cycle?

    @Query("SELECT * FROM specificDaysHourWeight WHERE taskId = :taskId")
    fun getTaskSpecificDays(taskId: Long): MutableList<SpecificDaysHourWeight>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(task: Task)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(specificDaysHourWeight: SpecificDaysHourWeight)

    @Query("SELECT * FROM task WHERE id = (SELECT MAX(id) FROM task)")
    fun getLastInserted(): Task?

    @Update
    fun update(task: Task)

    @Insert
    fun insertTake(takes: Takes)

    @Delete
    fun delete(task: Task)

    @Query("DELETE FROM task WHERE id = :taskId")
    fun deleteById(taskId: Long)

    @Query("DELETE FROM task")
    fun deleteAll()

}

