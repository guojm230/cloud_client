package com.example.cloud.page.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class UploadService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uri = intent?.getStringExtra("uri") ?: ""
        println("uri: $uri")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}