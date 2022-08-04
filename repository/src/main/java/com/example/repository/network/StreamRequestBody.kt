package com.example.repository.network

import android.content.Context
import android.net.Uri
import com.example.base.util.getFileLength
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream

fun Uri.asStreamRequestBody(context: Context): RequestBody {
    return StreamRequestBody(
        context.contentResolver.openInputStream(this)!!, getFileLength(context)
    )
}

class StreamRequestBody(
    val inputStream: InputStream, val length: Long
) : RequestBody() {
    //8M
    val buffer: ByteArray = ByteArray(1024 * 1024 * 8)

    override fun contentType(): MediaType? {
        return null
    }

    override fun writeTo(sink: BufferedSink) {
        var len = -1
        while (true) {
            len = inputStream.read(buffer)
            if (len == -1) {
                return
            }
            sink.write(buffer, 0, len)
        }
    }

    override fun contentLength(): Long {
        return length
    }


}