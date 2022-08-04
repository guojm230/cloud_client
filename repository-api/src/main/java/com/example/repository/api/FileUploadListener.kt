package com.example.repository.api

/**
 * 用于监听上传状态的listener
 */
interface FileUploadListener {

    fun onStart()

    /**
     * 上传进度变化时调用
     * @param uploaded 已经上传的大小
     * @param total 总大小，可能为-1
     */
    fun onProgress(uploaded: Long, total: Long)

    fun onSuccess()

    fun onError(e: Exception)

}