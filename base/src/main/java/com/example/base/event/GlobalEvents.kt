package com.example.base.event

import androidx.lifecycle.MutableLiveData

/**
 * 目前有很大问题
 * 1. 由于没有消息队列的存在，LiveData只能保存最新值，所以短时间发布多个事件时，只能获取最新值
 * 2. 由于粘性事件的存在，同样的事件会被重复触发，而SingleEvent可以保证事件只会被消费一次，
 *    但存在多个订阅者时就只会有一个订阅者收到事件
 * 对于方案2，解决办法就是修改LiveData的逻辑或者自己利用LifeCycle实现一套可以感知生命周期的回调
 */
object GlobalEvents {

    val uploadEvent: MutableLiveData<SingleEvent<UploadEvent>> = MutableLiveData()

}