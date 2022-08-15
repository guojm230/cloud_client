package com.example.repository

import androidx.lifecycle.Observer
import com.example.repository.api.task.FileTaskEvent
import com.example.repository.api.task.ProgressEvent
import com.example.repository.api.task.StateChangeEvent
import com.example.repository.task.AbstractFileTask
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class TaskTest {

    val task = object : AbstractFileTask<String>() {
        override fun id(): Int {
            TODO("Not yet implemented")
        }

        override fun taskInfo(): String {
            TODO("Not yet implemented")
        }

        override fun cancel() {
            TODO("Not yet implemented")
        }

        override fun pause() {
            TODO("Not yet implemented")
        }

        override fun resume() {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun baseTest() {
        var count = 0

        task.observe {
            when (it) {
                is StateChangeEvent<String> -> println(it.currentState)
                is ProgressEvent<String> -> count++
            }
        }

        val observer2 = Observer<FileTaskEvent<String>> {
            when (it) {
                is StateChangeEvent<String> -> println("state: " + it.currentState)
                is ProgressEvent<String> -> count++
            }
        }
        task.setStart()
        for (i in 1..10) {
            if (i % 2 == 0) {
                task.removeObserver(observer2)
            } else {
                task.observe(observer2)
            }
            task.updateProgress(i)
        }
        task.removeObserver(observer2)
        task.setSuccess()
        Assert.assertEquals(15, count)
    }

    @Test
    fun te() = runBlocking {
        val job = launch(start = CoroutineStart.LAZY) {
            println("123")
        }
        val job2 = launch {
            println("1")
            delay(1000)
            job.start()
        }
        job2.join()
    }

    @Test
    fun coroutineTest() = runBlocking {
        val coroutineContext = Dispatchers.IO + SupervisorJob()
        val coroutineScope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = SupervisorJob()
        }

        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
        }

        val job1 = coroutineScope.launch {
            throw RuntimeException()
        }
        val job2 = coroutineScope.launch {
            try {
                delay(1000)
                println("success")
            } catch (e: CancellationException) {
                e.printStackTrace()
                println("job2 is canceled")
            }
        }
        job1.join()
        job2.join()
        println("1111")
    }

    class CustomCancelEx(
        val flag: Int
    ) : CancellationException()

    @Test
    fun cancelTest() = runBlocking {
        val job = launch(Dispatchers.IO) {
            while (isActive) {
            }
            try {
                yield()
            } catch (e: CancellationException) {
                Assert.assertTrue(e is CustomCancelEx)
                val ce = e as CustomCancelEx
                Assert.assertEquals(1, e.flag)
                println("flag is ${ce.flag}")
            }
        }
        delay(100)
        job.cancel(CustomCancelEx(1))
        job.join()
    }

}