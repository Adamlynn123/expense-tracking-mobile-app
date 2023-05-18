package com.example.myexpensetracker

import androidx.room.TypeConverter
import java.util.*


class TypeConverters {

    // Convert from date to long when writing to database
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    // Convert from long to date when reading from database
    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}