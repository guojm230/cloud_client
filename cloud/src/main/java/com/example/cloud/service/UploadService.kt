package com.example.cloud.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.base.AppContext
import com.example.base.util.getFileName
import com.example.base.util.nextID
import com.example.cloud.R
import com.example.cloud.components.FileExistDialog
import com.example.cloud.model.FileExistDialogModel
import com.example.repository.api.FileRepository
import com.example.repository.api.FileUploadListener
import com.example.repository.api.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * 上传文件时调用的service
 * 需要负责的
 */
@AndroidEntryPoint
class UploadService () : Service() {

    @Inject
    lateinit var repository: FileRepository

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriStr = intent?.getStringExtra("uri") ?: ""
        val path = intent?.getStringExtra("path") ?: ""
        val overwrite = intent?.getBooleanExtra("overwrite",false) ?: false
        val uri = uriStr.toUri()

        coroutineScope.launch {
            val filePath = "$path/${uri.getFileName(applicationContext)}"
            if (checkFileExist(filePath)){
                FileExistDialog(AppContext.getActivityContext(), FileExistDialogModel(
                    filePath,
                    onConfirmCallback = {
                        coroutineScope.launch {
                            uploadFile(uri, path, true)
                        }
                    }
                )).show()
                return@launch
            }
            uploadFile(uri, path, overwrite)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun uploadFile(uri: Uri,path: String,overwrite: Boolean){
        val context = applicationContext
        val channelId = context.getString(com.example.base.R.string.notification_channel_id)
        val notificationBuilder =
            NotificationCompat.Builder(context, channelId)
                .setContentTitle("文件上传中")
                .setProgress(100,0,false)
                .setSmallIcon(R.drawable.icon_lark_logo).setPriority(NotificationCompat.PRIORITY_HIGH)
        val notificationId = nextID()

        repository.uploadFile(uri,path,overwrite,object : FileUploadListener{
            override fun onStart() {
                //create notification
                startForeground(notificationId,notificationBuilder.build())
                NotificationManagerCompat.from(context).notify(notificationId,notificationBuilder.build())
            }

            override fun onProgress(uploaded: Long, total: Long) {
                val percent = ((uploaded.toFloat()/total) * 100).toInt()
                notificationBuilder.setProgress(100,percent,false)
                NotificationManagerCompat.from(context).notify(notificationId,notificationBuilder.build())
            }

            override fun onSuccess() {
                notificationBuilder.setContentText("上传成功")
                notificationBuilder.setProgress(0,0,false)
                NotificationManagerCompat.from(context)
                    .notify(notificationId,notificationBuilder.build())
                stopForeground(false)
            }

            override fun onError(e: Exception) {
                notificationBuilder.setContentText("上传失败")
                notificationBuilder.setProgress(0,0,false)
                NotificationManagerCompat.from(context)
                    .notify(notificationId,notificationBuilder.build())
                stopForeground(false)
            }

        })
    }

    private suspend fun checkFileExist(path: String): Boolean{
        val item = repository.findFileItem(path).getResultOrNull()
        return item != null
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }


}