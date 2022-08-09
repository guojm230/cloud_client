package com.example.cloud.model

data class AlertDialogEvent(
    val title: String,
    val message: String,
    val onConfirmCallback: (()->Unit)? = null,
    val onCancelCallback: (()->Unit)? = null
)