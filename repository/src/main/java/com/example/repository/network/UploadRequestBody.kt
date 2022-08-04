package com.example.repository.network

import com.example.repository.api.FileUploadListener
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.buffer

/**
 * MultipartBody代理类，传入新的代理Sink，来统计上传进度
 * @see UploadMonitorSink
 */
class UploadRequestBody(
    val delegate: MultipartBody, val listener: FileUploadListener? = null
) : RequestBody() {

    override fun contentType(): MediaType {
        return delegate.contentType()
    }

    override fun writeTo(sink: BufferedSink) {
        listener?.onStart()
        val monitorSink = UploadMonitorSink(sink, delegate.contentLength(), listener).buffer()
        delegate.writeTo(monitorSink)
        monitorSink.flush()
    }

    override fun contentLength(): Long {
        return delegate.contentLength()
    }

    override fun isDuplex(): Boolean {
        return delegate.isDuplex()
    }

    override fun isOneShot(): Boolean {
        return delegate.isOneShot()
    }

}