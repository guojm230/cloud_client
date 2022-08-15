package com.example.repository.api.model

import java.time.LocalDateTime

data class UploadTaskInfo(
    val id: Int = 0,

    val accountId: Int,

    val userId: Int,

    val fileName: String,

    val fileUri: String,

    val uploadPath: String,

    val totalLength: Long,

    val uploadLength: Long,

    val createTime: LocalDateTime,

    val lastModifyTime: LocalDateTime
)