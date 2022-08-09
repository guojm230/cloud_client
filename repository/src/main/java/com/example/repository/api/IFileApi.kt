package com.example.repository.api

import android.net.Uri
import com.example.base.result.AsyncResult
import com.example.repository.api.model.FileItem

interface IFileApi{

    /**
     * 查找指定文件夹的路径
     */
    suspend fun queryFiles(userId:Int, path: String, token: String): AsyncResult<List<FileItem>>

    /**
     * 查询文件对应路径的文件信息
     * @param path 文件路径
     * @param userId 用户Id
     * @param token 账户token
     * @return 对应路径的文件信息
     */
    suspend fun queryFileItem(userId: Int,path: String,token: String): AsyncResult<FileItem>

    /**
     * 删除指定文件
     * @return 是否成功删除指定文件
     */
    suspend fun deleteFile(userId: Int,fileItem: FileItem,token: String): AsyncResult<Boolean>

    /**
     * 移动文件
     * @param userId 用户Id
     * @param token 用户凭证
     * @param from 被移动文件
     * @param to 移动文件夹
     * @param overwrite 是否覆盖同名文件
     * @return 返回新位置的文件信息
     * @throws
     */
    suspend fun moveFile(
        userId: Int,
        token: String,
        from: FileItem,
        to: FileItem,
        overwrite: Boolean
    ): AsyncResult<FileItem>

    fun uploadFile(
        userId: Int,
        token: String,
        uri: Uri,
        path: String,
        overwrite: Boolean,
        uploadListener: FileUploadListener
    )
}