package com.example.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.repository.dao.entity.UploadTaskEntity

@Dao
interface UploadTaskDao {

    @Insert
    suspend fun insertTask(taskEntity: UploadTaskEntity): Long

}