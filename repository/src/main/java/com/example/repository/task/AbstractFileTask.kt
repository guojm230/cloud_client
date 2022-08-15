package com.example.repository.task

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.repository.api.task.FileTask
import com.example.repository.api.task.FileTaskEvent
import com.example.repository.api.task.ProgressEvent
import com.example.repository.api.task.StateChangeEvent
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


/**
 * 可监听状态的任务
 */
abstract class AbstractFileTask<FileInfo>(
    initialState: Int = FileTask.STATE_INITIAL,
    initProgress: Int = 0
) : FileTask<FileInfo> {

    private val listeners = mutableSetOf<Observer<FileTaskEvent<FileInfo>>>()

    /**
     * 允许从外部恢复当前状态
     */
    private val state = AtomicInteger(initialState)
    private var progress: Int = initProgress

    override fun state(): Int {
        return state.get()
    }

    override fun progress(): Int {
        return progress
    }

    override fun removeObserver(observer: Observer<FileTaskEvent<FileInfo>>) {
        synchronized(this) {
            val iterator = listeners.iterator()
            while (iterator.hasNext()) {
                val obs = iterator.next()
                if (obs == observer) {
                    iterator.remove()
                    return
                }
            }
        }
    }

    override fun removeObserver(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<FileTaskEvent<FileInfo>>
    ) {
        synchronized(this) {
            val iterator = listeners.iterator()
            while (iterator.hasNext()) {
                val obs = iterator.next()
                if (obs is LifecycleObserverWrapper && obs.delegate == observer) {
                    obs.owner.lifecycle.removeObserver(obs)
                    iterator.remove()
                    return
                }
            }
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<FileTaskEvent<FileInfo>>
    ) {
        synchronized(this) {
            val exists = listeners.filterIsInstance<LifecycleObserverWrapper>()
                .find { it.delegate == observer }
            if (exists == null) {
                listeners.add(LifecycleObserverWrapper(lifecycleOwner, observer))
            }
        }
    }

    override fun observe(observer: Observer<FileTaskEvent<FileInfo>>) {
        synchronized(this) {
            if (!listeners.contains(observer)) {
                listeners.add(observer)
            }
        }
    }

    protected fun dispatchEvent(event: FileTaskEvent<FileInfo>) {
        synchronized(this) {
            listeners.forEach {
                it.onChanged(event)
            }
        }
    }

    fun updateProgress(newProgress: Int) {
        this.progress = newProgress
        dispatchEvent(ProgressEvent<FileInfo>(this, progress))
    }

    fun changeState(newState: Int, throwable: Throwable? = null) {
        if (!canModifyState()) {
            throw IllegalStateException("can't convert state from $state to $newState")
        }
        val lastState = state.get()
        var s = state.get()
        while (!state.compareAndSet(s, newState)) {
            s = state.get()
        }
        dispatchEvent(
            StateChangeEvent<FileInfo>(
                this, lastState, newState
            )
        )
    }

    fun setStart() {
        if (canModifyState()) {
            changeState(FileTask.STATE_RUNNING)
        }
    }

    fun setSuccess() {
        if (canModifyState()) {
            changeState(FileTask.STATE_SUCCESS)
        }
    }

    fun setCancel() {
        if (canModifyState()) {
            changeState(FileTask.STATE_CANCEL)
        }
    }

    fun setPause() {
        if (canModifyState()) {
            changeState(FileTask.STATE_PAUSE)
        }
    }

    fun setError(throwable: Throwable) {
        if (canModifyState()) {
            changeState(FileTask.STATE_ERROR, throwable)
        }
    }

    private fun canModifyState(): Boolean {
        return state.get() <= FileTask.STATE_PAUSE
    }

    private inner class LifecycleObserverWrapper(
        val owner: LifecycleOwner,
        val delegate: Observer<FileTaskEvent<FileInfo>>
    ) : LifecycleEventObserver, Observer<FileTaskEvent<FileInfo>> {

        var active = AtomicBoolean(false)

        init {
            owner.lifecycle.addObserver(this)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            val state = owner.lifecycle.currentState

            val newActive = state.isAtLeast(Lifecycle.State.STARTED)
            var prevActive = active.get()

            while (active.compareAndSet(prevActive, newActive)) {
                prevActive = active.get()
            }

            if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                removeObserver(delegate)
            }
        }

        override fun onChanged(t: FileTaskEvent<FileInfo>?) {
            if (active.get()) {
                delegate.onChanged(t)
            }
        }

    }

}