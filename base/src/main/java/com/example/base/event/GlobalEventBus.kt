@file:Suppress("UNCHECKED_CAST")

package com.example.base.event

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 可感知生命周期的简单消息总线
 * 线程安全
 */
object GlobalEventBus {

    private val TAG = GlobalEventBus::class.java.canonicalName

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newFixedThreadPool(2)

    private val observerMap = ConcurrentHashMap<String, MutableSet<ObserverWrapper<Any>>>()
    private val wrapperMap = ConcurrentHashMap<Observer<*>, ObserverWrapper<*>>()

    /**
     * 用来保证异步事件的有序性，当同类型的事件正在dispatching时，事件会进行等待
     * 同时避免后面的事件因为前面事件的同步锁而阻塞
     */
    private val dispatchingMap = ConcurrentHashMap<String, Boolean>()

    private val eventQueue = LinkedList<Pair<String, Any>>()
    private val lock = Object()

    private val initial = AtomicBoolean(false)

    fun init() {
        if (!initial.getAndSet(true)) {
            Thread(this::dispatch).start()
        }
    }

    inline fun <reified T : Any> subscribeOnUIThread(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<T>
    ) {
        subscribe(lifecycleOwner, ThreadMode.MAIN, observer)
    }

    inline fun <reified T : Any> subscribe(
        threadMode: ThreadMode = ThreadMode.UNCONFINED,
        observer: Observer<T>,
    ) {
        subscribe(T::class.java.canonicalName!!, observer, null, threadMode)
    }


    inline fun <reified T : Any> subscribe(
        lifecycleOwner: LifecycleOwner,
        threadMode: ThreadMode,
        observer: Observer<T>
    ) {
        subscribe(T::class.java.canonicalName!!, observer, lifecycleOwner, threadMode)
    }

    inline fun <reified T : Any> postEvent(event: T) {
        postEvent(T::class.java.canonicalName!!, event)
    }

    fun postEvent(key: String, event: Any) {
        synchronized(lock) {
            eventQueue.addLast(Pair(key, event))
            lock.notify()
        }
    }

    /**
     * 同步方式发布事件，不会经过消息队列,而是直接dispatch到ThreadMode为Sync和UNCONFINED的observer
     */
    inline fun <reified T : Any> postSyncEvent(event: T) {
        postSyncEvent(T::class.java.canonicalName!!, event)
    }

    fun postSyncEvent(key: String, event: Any) {
        val observers = observerMap[key]
        observers?.run {
            synchronized(observers) {
                val iterator = iterator()
                while (iterator.hasNext()) {
                    val observer = iterator.next()
                    if (observer.threadMode == ThreadMode.SYNC || observer.threadMode == ThreadMode.UNCONFINED) {
                        observer.onChanged(event)
                    }
                }
            }
        }
    }

    fun subscribe(
        key: String,
        observer: Observer<*>,
        owner: LifecycleOwner? = null,
        threadMode: ThreadMode
    ) {
        val wrapper = if (owner == null) {
            ObserverWrapper(key, threadMode, observer)
        } else {
            LifecycleObserverWrapper(owner, key, threadMode, observer)
        }
        wrapperMap[observer] = wrapper
        observerMap.putIfAbsent(key, mutableSetOf())
        val observers = observerMap[key]!!
        synchronized(observers) {
            observers.add(wrapper as ObserverWrapper<Any>)
        }
    }

    inline fun <reified T> unsubscribe(observer: Observer<T>) {
        unsubscribe(T::class.java.canonicalName!!, observer)
    }

    fun unsubscribe(key: String, observer: Observer<*>) {
        val wrapper = wrapperMap[observer] ?: return
        val observers = observerMap[key] ?: return
        wrapperMap.remove(observer)
        synchronized(observers) {
            observers.remove(wrapper)
        }
    }


    private fun dispatch() {
        try {
            while (true) {
                var event: Pair<String, Any>? = null
                synchronized(lock) {
                    while (eventQueue.isEmpty()) {
                        lock.wait()
                    }
                    val iterator = eventQueue.iterator()
                    while (iterator.hasNext()) {
                        val e = iterator.next()
                        if (dispatchingMap[e.first] == true) {
                            continue
                        }
                        iterator.remove()
                        dispatchingMap[e.first] = true
                        event = e
                        break
                    }
                }
                if (event != null) {
                    val (key, value) = event!!
                    val observers = observerMap[key]
                    observers?.run {
                        synchronized(observers) {
                            val iterator = observers.iterator()
                            while (iterator.hasNext()) {
                                val ob = iterator.next()
                                when (ob.threadMode) {
                                    ThreadMode.MAIN -> mainHandler.post { ob.onChanged(value) }
                                    ThreadMode.BACKGROUND, ThreadMode.UNCONFINED -> executor.submit {
                                        ob.onChanged(
                                            value
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                    dispatchingMap[key] = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "init: ", e)
        }
    }

    open class ObserverWrapper<T>(
        val key: String,
        val threadMode: ThreadMode,
        val delegate: Observer<T>
    ) : Observer<T> by delegate {}

    class LifecycleObserverWrapper<T>(
        val owner: LifecycleOwner,
        key: String,
        threadMode: ThreadMode,
        delegate: Observer<T>
    ) : GlobalEventBus.ObserverWrapper<T>(key, threadMode, delegate), LifecycleEventObserver {

        var active = AtomicBoolean(false)

        init {
            owner.lifecycle.addObserver(this)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            val state = owner.lifecycle.currentState

            val newActive = state.isAtLeast(Lifecycle.State.STARTED)
            var prevActive = active.get()

            while (!active.compareAndSet(prevActive, newActive)) {
                prevActive = active.get()
            }

            if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                unsubscribe(key, delegate)
            }
        }

        override fun onChanged(t: T) {
            if (active.get()) {
                delegate.onChanged(t)
            }
        }

    }
}

enum class ThreadMode {
    MAIN,   //运行在主线程
    BACKGROUND, //运行在后台线程
    SYNC,  //同步调用,和调用者同线程
    UNCONFINED //任意的，同步事件和异步事件都会调用
}