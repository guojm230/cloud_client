package com.example.repository.api.model

data class FileItem( //唯一路径标识
    val path: String, val name: String, //MIME type
    val type: String, val childrenSize: Int
)