package com.example.myexpensetracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "transactions")
@androidx.room.TypeConverters(TypeConverters::class)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val tv_label: String,
    val tv_amount: Double,
    val category: String,
    val description: String,
    @ColumnInfo(name = "date", defaultValue = "0") val date: Long = System.currentTimeMillis()): java.io.Serializable{}
