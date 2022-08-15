package com.example.repository.api

import android.net.Uri
import com.example.repository.api.model.UploadTaskInfo
import com.example.repository.api.task.FileTask

/**
 * 负责所有的上传文件任务管理
 */
interface FileTaskManager {

    /**
     * 上传文件
     * @param uri 本地文件的uri
     * @param path 上传文件夹的path路径
     * @param overwrite 文件冲突时是否覆盖
     */
    suspend fun uploadFile(uri: Uri, path: String, overwrite: Boolean): FileTask<UploadTaskInfo>

    suspend fun findAllUploadTask(): List<FileTask<UploadTaskInfo>>

}