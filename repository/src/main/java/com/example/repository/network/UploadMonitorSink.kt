package com.example.repository.network

import com.example.repository.api.FileUploadListener
import okio.Buffer
import okio.ForwardingSink
import okio.Sink

/**
 * 重写write方法，监听写入的数据量
 */
class UploadMonitorSink(
    delegate: Sink, val contentLength: Long, val listener: FileUploadListener? = null
) : ForwardingSink(delegate) {

    private var writeCount = 0L

    override fun write(source: Buffer, byteCount: Long) {
        super.write(source, byteCount)
        writeCount += byteCount
        listener?.onProgress(byteCount, contentLength)
    }

}