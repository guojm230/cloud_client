package com.example.repository.api.model

data class FileItem(
    val path: String, //唯一路径标识
    val name: String,
    val length: Long,
    val lastModified: Long,
    val isDirectory: Boolean, //MIME type
    val type: String,
    val childrenSize: Int
)