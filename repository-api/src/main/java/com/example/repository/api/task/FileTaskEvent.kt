package com.example.repository.api.task

sealed interface FileTaskEvent<T> {
    val task: FileTask<T>
}

data class StateChangeEvent<T>(
    override val task: FileTask<T>,
    val lastState: Int,
    val currentState: Int,
    val throwable: Throwable? = null
) : FileTaskEvent<T>

data class ProgressEvent<T>(
    override val task: FileTask<T>,
    val progress: Int
) : FileTaskEvent<T>