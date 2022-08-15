package com.example.repository.api.task

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 * 代表一个上传/下载文件的任务
 * @param TaskInfo 文件信息
 */
interface FileTask<TaskInfo> {

    /**
     * 任务Id
     */
    fun id(): Int

    fun taskInfo(): TaskInfo

    /**
     * 任务当前状态
     */
    fun state(): Int

    fun progress(): Int

    fun cancel()

    fun pause()

    fun resume()

    fun observe(observer: Observer<FileTaskEvent<TaskInfo>>)

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<FileTaskEvent<TaskInfo>>)

    fun removeObserver(observer: Observer<FileTaskEvent<TaskInfo>>)

    fun removeObserver(lifecycleOwner: LifecycleOwner, observer: Observer<FileTaskEvent<TaskInfo>>)


    companion object {
        const val STATE_INITIAL = 0
        const val STATE_RUNNING = 1
        const val STATE_PAUSE = 2
        const val STATE_SUCCESS = 3
        const val STATE_CANCEL = 4
        const val STATE_ERROR = 5
    }

}