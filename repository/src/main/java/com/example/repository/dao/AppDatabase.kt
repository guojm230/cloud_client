package com.example.repository.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.repository.entity.LoginAccount

@Database(
    entities = [LoginAccount::class], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}