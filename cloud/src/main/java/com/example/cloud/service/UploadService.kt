package com.example.cloud.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.base.event.*
import com.example.base.util.nextID
import com.example.cloud.R
import com.example.repository.api.FileRepository
import com.example.repository.api.FileUploadListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import javax.inject.Inject

/**
 * 上传文件时调用的service
 * 需要负责的
 */
@AndroidEntryPoint
class UploadService() : Service() {

    @Inject
    lateinit var repository: FileRepository

    val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriStr = intent?.getStringExtra("uri") ?: ""
        val path = intent?.getStringExtra("path") ?: ""
        val overwrite = intent?.getBooleanExtra("overwrite", false) ?: false
        val uri = uriStr.toUri()

        uploadFile(uri, path, overwrite)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun uploadFile(uri: Uri, path: String, overwrite: Boolean) {
        val context = applicationContext
        val channelId = context.getString(com.example.base.R.string.notification_channel_id)
        val notificationBuilder =
            NotificationCompat.Builder(context, channelId)
                .setContentTitle("文件上传中")
                .setProgress(100, 0, false)
                .setSmallIcon(R.drawable.icon_lark_logo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        val notificationId = nextID()

        repository.uploadFile(uri, path, overwrite, object : FileUploadListener {
            override fun onStart() {
                //create notification
                startForeground(notificationId, notificationBuilder.build())
                NotificationManagerCompat.from(context)
                    .notify(notificationId, notificationBuilder.build())
                GlobalEventBus.postEvent<UploadEvent>(UploadStartEvent(uri, path))
            }

            override fun onProgress(uploaded: Long, total: Long) {
                val percent = ((uploaded.toFloat() / total) * 100).toInt()
                notificationBuilder.setProgress(100, percent, false)
                NotificationManagerCompat.from(context)
                    .notify(notificationId, notificationBuilder.build())
                GlobalEventBus.postEvent<UploadEvent>(
                    UploadProgressEvent(
                        uri, path, percent
                    )
                )
            }

            override fun onSuccess() {
                notificationBuilder.setContentText("上传成功")
                notificationBuilder.setProgress(0, 0, false)
                NotificationManagerCompat.from(context)
                    .notify(notificationId, notificationBuilder.build())
                stopForeground(false)
                GlobalEventBus.postEvent<UploadEvent>(UploadSuccessEvent(uri, path))
            }

            override fun onError(e: Exception) {
                notificationBuilder.setContentText("上传失败")
                notificationBuilder.setProgress(0, 0, false)
                NotificationManagerCompat.from(context)
                    .notify(notificationId, notificationBuilder.build())
                stopForeground(false)
                GlobalEventBus.postEvent<UploadEvent>(UploadErrorEvent(uri, path, e))
            }

        })
    }

    override fun onDestroy() {
        uiScope.cancel()
        super.onDestroy()
    }


}