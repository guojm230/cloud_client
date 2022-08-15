package com.example.repository.task

import android.content.Context
import androidx.core.net.toUri
import com.example.repository.api.model.UploadTaskInfo
import com.example.repository.dao.UploadTaskDao
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.BufferedInputStream
import java.util.concurrent.Executors

class UploadFileWorker(
    val context: Context,
    val coroutineScope: CoroutineScope,
    val okHttpClient: OkHttpClient,
    val uploadTaskDao: UploadTaskDao,
    val task: AbstractFileTask<UploadTaskInfo>
) {
    val job: Job = createJob()
    var chunkSize = CHUNK_SIZE

    private var uploadLength = 0L
    private var inputStream: BufferedInputStream? = null


    fun start(): Job {
        job.start()
        return job
    }

    private fun createJob(): Job {
        return coroutineScope.launch(start = CoroutineStart.LAZY) {
            while (isActive) {
                uploadOnce()
            }
            try {
                yield()
            } catch (e: CancellationException) {

            }
        }
    }

    /**
     * 上传一个chunk
     */
    private suspend fun uploadOnce() = suspendCancellableCoroutine<Unit> { con ->
        executor.submit {
            if (inputStream == null) {
                initStream()
            }
            
        }
    }

    private fun initStream() {
        val uri = task.taskInfo().fileUri.toUri()
        inputStream = BufferedInputStream(context.contentResolver.openInputStream(uri)!!)
        inputStream!!.skip(task.taskInfo().uploadLength)
        uploadLength = task.taskInfo().uploadLength
    }

    companion object {
        const val CHUNK_SIZE = 1024 * 4   //4kb
        private val executor = Executors.newFixedThreadPool(2)
    }

}