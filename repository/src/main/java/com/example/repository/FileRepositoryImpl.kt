package com.example.repository

import android.content.Context
import android.net.Uri
import com.example.base.result.AsyncResult
import com.example.base.result.Error
import com.example.base.result.ErrorCode.INVALID_TOKEN
import com.example.base.result.ErrorCode.NOT_FOUND
import com.example.base.result.Success
import com.example.repository.api.*
import com.example.repository.api.model.FileItem
import com.example.repository.api.model.UploadTaskInfo
import com.example.repository.api.task.FileTask
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
    val userRepository: UserRepository,
    val okHttpClient: OkHttpClient,
    val fileApi: IFileApi
) : FileRepository {

    override suspend fun findFileItem(path: String): AsyncResult<FileItem?> {
        if (!userRepository.isAuthenticated()) {
            return AsyncResult.fail(INVALID_TOKEN)
        }

        val token = userRepository.currentAccountToken()!!
        val currentUser = userRepository.currentUser()!!
        val result = fileApi.queryFileItem(currentUser.id, path, token)

        return when (result) {
            is Success -> AsyncResult.success(result.data)
            is Error -> {
                if (result.code == NOT_FOUND) {
                    AsyncResult.success(null)
                } else {
                    result as AsyncResult<FileItem?>
                }
            }
        }
    }

    override suspend fun findFiles(path: String): AsyncResult<List<FileItem>> {
        if (!userRepository.isAuthenticated()) {
            return AsyncResult.fail(INVALID_TOKEN)
        }
        val token = userRepository.currentAccountToken()!!
        val currentUser = userRepository.currentUser()!!
        return fileApi.queryFiles(currentUser.id, path, token)
    }

    override suspend fun deleteFile(fileItem: FileItem): AsyncResult<Boolean> {
        val token = userRepository.currentAccountToken()!!
        val currentUser = userRepository.currentUser()!!
        return fileApi.deleteFile(currentUser.id, fileItem, token)
    }

    override suspend fun moveFile(
        from: FileItem, to: FileItem, overwrite: Boolean
    ): AsyncResult<FileItem> {
        if (userRepository.currentUser() == null) {
            return AsyncResult.fail(INVALID_TOKEN)
        }
        val currentUser = userRepository.currentUser()!!
        val token = userRepository.currentAccountToken()!!
        return fileApi.moveFile(currentUser.id, token, from, to, overwrite)
    }

    override fun uploadFile(
        uri: Uri, path: String, overwrite: Boolean, uploadListener: FileUploadListener
    ) {
        val currentUser = userRepository.currentUser()!!
        val token = userRepository.currentAccountToken()!!
        fileApi.uploadFile(currentUser.id, token, uri, path, overwrite, uploadListener)
    }

    override fun downloadFile(fileItem: FileItem, listener: FileDownloadListener) {
        //TODO
    }

    override suspend fun upload(): FileTask<UploadTaskInfo> {

        TODO("Not yet implemented")
    }


}