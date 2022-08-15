package com.example.repository.task

import com.example.repository.api.task.FileTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class AbstractCoroutineFileTask<FileInfo>(
    coroutineScope: CoroutineScope,
    initialState: Int = FileTask.STATE_INITIAL,
    initProgress: Int = 0
) : AbstractFileTask<FileInfo>(initialState, initProgress) {

    var job: Job? = null

    override fun pause() {
        job?.cancel()
        job = null
    }

    override fun cancel() {
        job?.cancel()
        job = null
    }

    override fun resume() {
        
    }
}