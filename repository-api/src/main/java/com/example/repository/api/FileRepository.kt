package com.example.repository.api

import android.net.Uri
import com.example.base.result.AsyncResult
import com.example.repository.api.model.FileItem
import com.example.repository.api.model.UploadTaskInfo
import com.example.repository.api.task.FileTask

interface FileRepository {

    suspend fun findFileItem(path: String): AsyncResult<FileItem?>

    /**
     * 查找对应文件
     */
    suspend fun findFiles(path: String = ""): AsyncResult<List<FileItem>>

    suspend fun deleteFile(fileItem: FileItem): AsyncResult<Boolean>

    /**
     * 移动文件
     * @param from 被移动的文件
     * @param to 目标文件夹
     * @param overwrite 目标文件名已经存在时是否覆盖
     */
    suspend fun moveFile(from: FileItem, to: FileItem, overwrite: Boolean): AsyncResult<FileItem>

    fun uploadFile(
        uri: Uri, path: String, overwrite: Boolean, uploadListener: FileUploadListener
    )

    fun downloadFile(fileItem: FileItem, listener: FileDownloadListener)

    suspend fun upload(): FileTask<UploadTaskInfo>

}