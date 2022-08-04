package com.example.base.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile


fun Uri.getFileName(context: Context): String {
    return when (scheme) {
        "file" -> toFile().name
        "content" -> {
            val resolver = context.contentResolver
            val cusor = resolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null)
                ?: throw java.lang.IllegalArgumentException("Can't query uri(${this}) info from Content Provider")
            cusor.moveToFirst()
            return cusor.getString(cusor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
        else -> throw IllegalArgumentException("Don't support $scheme scheme")
    }
}


fun Uri.getFileLength(context: Context): Long {
    return when (scheme) {
        "file" -> toFile().length()
        "content" -> {
            val resolver = context.contentResolver
            val cusor = resolver.query(this, arrayOf(OpenableColumns.SIZE), null, null) ?: return -1
            cusor.moveToFirst()
            return cusor.getLong(cusor.getColumnIndex(OpenableColumns.SIZE))
        }
        else -> throw IllegalArgumentException("Don't support $scheme scheme")
    }
}