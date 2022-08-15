package com.example.user.common

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.base.util.nextID
import com.example.user.R

fun notifyVerifyCode(context: Context, code: String) {
    val channelId = context.getString(com.example.base.R.string.notification_channel_id)
    val text = context.getString(R.string.notification_verify_code_template).run {
        replace("\${code}", code)
    }
    val notification =
        NotificationCompat.Builder(context, channelId).setContentTitle("验证码")
            .setContentText(text)
            .setSmallIcon(R.drawable.logo).setPriority(NotificationCompat.PRIORITY_HIGH).build()
    NotificationManagerCompat.from(context).notify(nextID(), notification)
}