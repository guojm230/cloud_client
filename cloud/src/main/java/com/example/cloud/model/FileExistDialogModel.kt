package com.example.cloud.model

data class FileExistDialogModel(
    val filePath: String,
    val onConfirmCallback: (()->Unit)? = null,
    val onCancelCallback: (()->Unit)? = null
)