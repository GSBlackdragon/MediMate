package com.example.mms.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.mms.model.medicines.Medicine
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Locale


@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["code_cis"],
            childColumns = ["medicineCIS"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["email"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Doctor::class,
            parentColumns = ["rpps"],
            childColumns = ["doctor"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var type: String,

    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var startDate: LocalDateTime = LocalDateTime.now(),
    var endDate: LocalDateTime = LocalDateTime.now(),
    var stopOnStock : Boolean = false,




    @ColumnInfo(index = true)
    var medicineCIS: Long,

    @ColumnInfo(index = true)
    var userId: String,

    @Ignore
    var cycle: Cycle = Cycle(),

    @Ignore
    var specificDays: MutableList<SpecificDaysHourWeight> = mutableListOf(),

    @Ignore
    var oneTakeHourWeight: HourWeight? = null,

    @ColumnInfo(index = true)
    var doctor: Long? = null



) {
    constructor() : this(0, "", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),LocalDateTime.now(), false,0, "", Cycle(), mutableListOf())

    /**
     * The task is not empty, if the cycle is not empty, the specific days are not empty or the one take hour weight is not null
     */
    fun isNotEmpty(): Boolean {
        return !this.isEmpty()
    }

    /**
     * The task is empty, if the cycle is empty, the specific days are empty and the one take hour weight is null
     */
    fun isEmpty(): Boolean {
        return this.cycle.isEmpty() && this.specificDays.isEmpty() && this.oneTakeHourWeight == null
    }

    /**
     * String representation of the task
     *
     * @return the string representation of the task
     */
    override fun toString(): String {
        return "$id $medicineCIS: cycle ${this.cycle.isEmpty()}, specificdays ${this.specificDays.size}"
    }
}
