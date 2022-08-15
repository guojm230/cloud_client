package com.example.repository.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.repository.dao.entity.LoginAccount
import com.example.repository.dao.entity.UploadTaskEntity

@Database(
    entities = [LoginAccount::class, UploadTaskEntity::class], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    abstract fun uploadTaskDao(): UploadTaskDao

}