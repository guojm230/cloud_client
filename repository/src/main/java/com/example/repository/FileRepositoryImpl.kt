package com.example.repository

import com.example.repository.api.FileRepository
import com.example.repository.api.Result
import com.example.repository.api.ResultCode.INVALID_TOKEN
import com.example.repository.api.UploadListener
import com.example.repository.api.UserRepository
import com.example.repository.api.model.FileItem
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    val userRepository: UserRepository, val okHttpClient: OkHttpClient
) : FileRepository {
    override suspend fun findFiles(path: String): Result<List<FileItem>> {
        if (!userRepository.isAuthenticated()) {
            return Result.fail(INVALID_TOKEN)
        }
        val token = userRepository.currentAccountToken()!!
        val currentUser = userRepository.currentUser()
        return okHttpClient.callApi(QueryFilesApi("${currentUser!!.id}${path}", token))
    }

    override suspend fun deleteFile(fileItem: FileItem): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadFile(file: File, uploadListener: UploadListener) {
        TODO("Not yet implemented")
    }


}