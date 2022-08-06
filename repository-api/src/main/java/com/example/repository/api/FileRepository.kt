package com.example.repository.api

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.repository.api.model.FileItem

interface FileRepository {

    /**
     * 查找对应文件
     */
    suspend fun findFiles(path: String = ""): Result<List<FileItem>>

    suspend fun deleteFile(fileItem: FileItem): Result<Boolean>

    suspend fun moveFile(from: FileItem, to: FileItem, overwrite: Boolean): Result<Boolean>

    fun uploadFile(
        uri: Uri, path: String, overwrite: Boolean, uploadListener: FileUploadListener
    )

    fun downloadFile(fileItem: FileItem, listener: FileDownloadListener)

}