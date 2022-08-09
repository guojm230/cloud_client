package com.example.repository.api

import android.content.Context
import android.net.Uri
import com.example.base.result.AsyncResult
import com.example.base.util.getFileName
import com.example.repository.*
import com.example.repository.SERVER_URL
import com.example.repository.api.exceptions.ApiException
import com.example.repository.api.model.FileItem
import com.example.repository.network.UploadRequestBody
import com.example.repository.network.asStreamRequestBody
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.*
import java.io.IOException
import javax.inject.Inject

class FileApiImpl @Inject constructor(
    @ApplicationContext
    val context: Context,
    val okHttpClient: OkHttpClient
    ) : IFileApi {

    override suspend fun queryFiles(
        userId: Int,
        path: String,
        token: String
    ): AsyncResult<List<FileItem>> {
        val request = Request.Builder().run {
            withBaseUrl("/files/$userId$path")
            addToken(token)
            build()
        }
        return okHttpClient.call(request)
    }

    override suspend fun queryFileItem(userId: Int, path: String, token: String): AsyncResult<FileItem> {
        val request = Request.Builder().run {
            withBaseUrl("/file/item/${userId}/${path}")
            addToken(token)
            build()
        }
        return okHttpClient.call(request)
    }

    override suspend fun deleteFile(userId: Int, fileItem: FileItem, token: String): AsyncResult<Boolean> {
        val request = Request.Builder().run {
            withBaseUrl("/file/$userId/${fileItem.path}")
            delete()
            build()
        }
        return okHttpClient.call(request)
    }

    override suspend fun moveFile(
        userId: Int,
        token: String,
        from: FileItem,
        to: FileItem,
        overwrite: Boolean
    ): AsyncResult<FileItem> {
        val request = Request.Builder().run {
            withBaseUrl("/file/${userId}/move")
            addToken(token)
            post(
                mapOf(
                    "from" to from.path, "to" to to.path, "overwrite" to overwrite
                ).toJsonRequestBody()
            )
            build()
        }
        return okHttpClient.call(request)
    }

    override fun uploadFile(
        userId: Int,
        token: String,
        uri: Uri,
        path: String,
        overwrite: Boolean,
        uploadListener: FileUploadListener
    ) {
        val requestBody = MultipartBody.Builder().run {
            addFormDataPart("userId", userId.toString())
            addFormDataPart("path", path)
            addFormDataPart("overwrite", overwrite.toString())
            addFormDataPart("file", uri.getFileName(context), uri.asStreamRequestBody(context))
            build()
        }
        val request = Request.Builder().run {
            url("${SERVER_URL}/file/upload")
            addToken(token)
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
}