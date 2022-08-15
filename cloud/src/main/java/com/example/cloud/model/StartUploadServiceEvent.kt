package com.example.cloud.model

import android.net.Uri

data class StartUploadServiceEvent(
    val uri: Uri,
    val path: String,
    val overwrite: Boolean
)
