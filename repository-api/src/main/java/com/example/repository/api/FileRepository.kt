package com.example.repository.api

import com.example.repository.api.model.FileItem
import java.io.File

interface FileRepository {

    /**
     * 查找对应文件
     */
    suspend fun findFiles(path: String = ""): Result<List<FileItem>>

    suspend fun deleteFile(fileItem: FileItem): Result<Boolean>

    suspend fun uploadFile(file: File, uploadListener: UploadListener)
}