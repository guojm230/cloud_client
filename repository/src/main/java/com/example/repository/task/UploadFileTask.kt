package com.example.repository.task

import com.example.repository.api.model.UploadTaskInfo

class UploadFileTask(

    var info: UploadTaskInfo
) : AbstractFileTask<UploadTaskInfo>() {

    override fun id(): Int {
        return info.id
    }

    override fun cancel() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun taskInfo(): UploadTaskInfo {
        return info
    }

}