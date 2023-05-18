package com.example.myexpensetracker

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

// Setting up the room database and giving reference to the transaction data class
@Database(entities = [Transaction::class], version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    //Abstract function that returns the data access object for transactions
    abstract fun transactionDao() : TransactionDao

}