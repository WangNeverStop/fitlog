package com.fitlog.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/** Room type converters. Stores [LocalDate] as an epoch-day Long. */
class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()
}
