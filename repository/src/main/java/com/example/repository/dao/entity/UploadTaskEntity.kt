package com.example.repository.dao.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upload_task")
data class UploadTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "account_id")
    val accountId: Int,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "file_name")
    val fileName: String,
    @ColumnInfo(name = "file_uri")
    val fileUri: String,
    @ColumnInfo(name = "upload_path")
    val uploadPath: String,
    @ColumnInfo(name = "total_length")
    val totalLength: Long,
    @ColumnInfo(name = "upload_length")
    val uploadLength: Long,
    @ColumnInfo(name = "create_time")
    val createTime: Long,
    @ColumnInfo(name = "last_modify_time")
    val lastModifyTime: Long,
    @ColumnInfo(name = "last_state")
    val lastState: Int,
    val overwrite: Boolean
)