package com.example.repository

import android.content.Context
import android.net.Uri
import com.example.base.util.getFileLength
import com.example.base.util.getFileName
import com.example.repository.api.FileTaskManager
import com.example.repository.api.UserRepository
import com.example.repository.api.model.UploadTaskInfo
import com.example.repository.api.task.FileTask
import com.example.repository.dao.UploadTaskDao
import com.example.repository.dao.entity.UploadTaskEntity
import com.example.repository.task.AbstractFileTask
import com.example.repository.task.UploadFileWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * 负责上传/下载文件的类
 */
class FileTaskManagerImpl @Inject constructor(
    @ApplicationContext
    val context: Context,
    val uploadTaskDao: UploadTaskDao,
    val userRepository: UserRepository,
    val okHttpClient: OkHttpClient
) : FileTaskManager {

    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val maxUploadTaskSize = 4

    override suspend fun uploadFile(
        uri: Uri,
        path: String,
        overwrite: Boolean
    ): FileTask<UploadTaskInfo> {
        val taskEntity = saveAndCreateTaskEntity(uri, path, overwrite)
        val taskInfo = createUploadTaskInfo(taskEntity)
        val task = createTask(taskInfo)
        task.resume()
        return task
    }

    override suspend fun findAllUploadTask(): List<FileTask<UploadTaskInfo>> {
        TODO("Not yet implemented")
    }

    private fun createTask(taskInfo: UploadTaskInfo): FileTask<UploadTaskInfo> {
        return object : AbstractFileTask<UploadTaskInfo>() {
            val taskInfo = taskInfo
            var job: Job? = null

            override fun id(): Int {
                return taskInfo.id
            }

            override fun taskInfo(): UploadTaskInfo {
                return taskInfo
            }

            override fun cancel() {
                job?.cancel()
            }

            override fun pause() {
                job?.cancel()
            }

            override fun resume() {
                job = UploadFileWorker(
                    context, coroutineScope,
                    okHttpClient,
                    uploadTaskDao, this
                ).start()
            }
        }
    }

    private fun createUploadTaskInfo(taskEntity: UploadTaskEntity): UploadTaskInfo {
        return UploadTaskInfo(
            id = taskEntity.id,
            accountId = taskEntity.accountId,
            userId = taskEntity.userId,
            fileName = taskEntity.fileName,
            fileUri = taskEntity.fileUri,
            uploadLength = taskEntity.uploadLength,
            uploadPath = taskEntity.uploadPath,
            totalLength = taskEntity.totalLength,
            createTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(taskEntity.createTime),
                ZoneId.systemDefault()
            ),
            lastModifyTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(taskEntity.lastModifyTime),
                ZoneId.systemDefault()
            )
        )
    }

    private suspend fun saveAndCreateTaskEntity(
        uri: Uri,
        path: String,
        overwrite: Boolean
    ): UploadTaskEntity {
        val fileName = uri.getFileName(context)
        val fileLength = uri.getFileLength(context)

        val currentUser = userRepository.currentUser()!!

        val taskEntity = UploadTaskEntity(
            id = 0,
            accountId = currentUser.accountId,
            userId = currentUser.id,
            fileName = fileName,
            totalLength = fileLength,
            fileUri = uri.toString(),
            uploadPath = path,
            uploadLength = 0,
            createTime = System.currentTimeMillis(),
            lastModifyTime = System.currentTimeMillis(),
            lastState = FileTask.STATE_INITIAL,
            overwrite = overwrite
        )
        val id = uploadTaskDao.insertTask(taskEntity)
        return taskEntity.copy(id = id.toInt())
    }


}