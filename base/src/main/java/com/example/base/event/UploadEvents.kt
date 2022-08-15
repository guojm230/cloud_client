package com.example.base.event

import android.net.Uri

sealed interface UploadEvent

data class UploadStartEvent(
    val uri: Uri,
    val uploadPath: String
) : UploadEvent

data class UploadSuccessEvent(
    val uri: Uri,
    val uploadPath: String
) : UploadEvent

class UploadErrorEvent(
    val uri: Uri,
    val uploadPath: String,
    val throwable: Throwable
) : UploadEvent

class UploadProgressEvent(
    val uri: Uri,
    val uploadPath: String,
    val progress: Int
) : UploadEvent