package com.example.repository

import android.content.Context
import android.net.Uri
import com.example.base.util.getFileName
import com.example.repository.api.FileDownloadListener
import com.example.repository.api.FileRepository
import com.example.repository.api.FileUploadListener
import com.example.repository.api.Result
import com.example.repository.api.ResultCode.INVALID_TOKEN
import com.example.repository.api.UserRepository
import com.example.repository.api.exceptions.ApiException
import com.example.repository.api.model.FileItem
import com.example.repository.network.UploadRequestBody
import com.example.repository.network.asStreamRequestBody
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
    val userRepository: UserRepository,
    val okHttpClient: OkHttpClient
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

    override suspend fun moveFile(
        from: FileItem, to: FileItem, overwrite: Boolean
    ): Result<Boolean> {
        if (userRepository.currentUser() == null) {
            return Result.fail(INVALID_TOKEN)
        }
        val currentUser = userRepository.currentUser()!!
        val token = userRepository.currentAccountToken()!!
        return okHttpClient.callApi(MoveFileApi(currentUser.id, token, from, to, overwrite)).map {
            it!!["success"]
        }
    }

    override fun uploadFile(
        uri: Uri, path: String, overwrite: Boolean, uploadListener: FileUploadListener
    ) {
        val requestBody = MultipartBody.Builder().run {
            addFormDataPart("userId", userRepository.currentUser()!!.id.toString())
            addFormDataPart("path", path)
            addFormDataPart("overwrite", overwrite.toString())
            addFormDataPart("file", uri.getFileName(context), uri.asStreamRequestBody(context))
            build()
        }
        val request = Request.Builder().run {
            url("${SERVER_URL}/file/upload")
            addToken(userRepository.currentAccountToken()!!)
            post(UploadRequestBody(requestBody, uploadListener))
            build()
        }

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                uploadListener.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    uploadListener.onSuccess()
                } else {
                    uploadListener.onError(ApiException())
                }
            }

        })
    }

    override fun downloadFile(fileItem: FileItem, listener: FileDownloadListener) {

    }


}