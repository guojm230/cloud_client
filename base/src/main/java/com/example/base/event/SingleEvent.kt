package com.example.base.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 代表只能被单次消费的事件
 * 避免被LiveData中数据回调被多次触发
 */
class SingleEvent<T>(val data: T) {
    var isConsumed: AtomicBoolean = AtomicBoolean(false)
        private set

    fun consume(consumer: ((T)->Unit)){
        if(isConsumed.compareAndSet(false,true)){
            consumer(data)
        }
    }
}

fun <T> LiveData<SingleEvent<T>>.consume(owner: LifecycleOwner, block: ((T)->Unit)){
    observe(owner){
        it.consume { data->
            block(data)
        }
    }
}

fun <T> MutableLiveData<SingleEvent<T>>.postEvent(e: T){
    postValue(SingleEvent(e))
}