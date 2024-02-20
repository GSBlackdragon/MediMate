package com.example.mms.database.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Converts a [LocalDateTime] to a [Long] and vice versa.
 * This is needed because Room does not support [LocalDateTime]
 * Is used for storing the [LocalDateTime] in the database.
 */
@ProvidedTypeConverter
class DateConverter {

    /**
     * Converts a [Long] to a [LocalDateTime]
     *
     * @param value The [Long] to convert
     * @return The converted [LocalDateTime]
     */
    @TypeConverter
    fun fromTimestamp(value: Long): LocalDateTime {
        return LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC)
    }

    /**
     * Converts a [LocalDateTime] to a [Long]
     *
     * @param date The [LocalDateTime] to convert
     * @return The converted [Long]
     */
    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime): Long {
        return date.toEpochSecond(ZoneOffset.UTC)
    }
}
