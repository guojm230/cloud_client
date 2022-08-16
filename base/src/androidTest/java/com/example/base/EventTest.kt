package com.example.base

import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.base.event.GlobalEventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class EventTest {
    @Test
    fun asyncTest() = runBlocking {
        GlobalEventBus.init()
        var count = 0
        val observer = Observer<String> {
            count++
        }
        GlobalEventBus.subscribe(observer = observer)
        delay(10)
        GlobalEventBus.postEvent("123")
        delay(10)
        GlobalEventBus.unsubscribe(observer)
        delay(10)
        GlobalEventBus.postEvent("1")
        delay(10)
        Assert.assertEquals(1, count)
        GlobalEventBus.subscribe(observer = observer)
        GlobalEventBus.postEvent("1")
        delay(10)
        Assert.assertEquals(2, count)
    }

    @Test
    fun syncTest() {
        GlobalEventBus.init()
        var count = 0
        val observer = Observer<String> {
            count++
        }
        GlobalEventBus.subscribe(GlobalEventBus.ThreadMode.SYNC, observer = observer)
        GlobalEventBus.postSyncEvent("123")
        GlobalEventBus.unsubscribe(observer)
        GlobalEventBus.postSyncEvent("1")
        Assert.assertEquals(1, count)
        GlobalEventBus.subscribe(threadMode = GlobalEventBus.ThreadMode.SYNC, observer = observer)
        GlobalEventBus.postSyncEvent("1")
        Assert.assertEquals(2, count)
    }

    @Test
    fun threadSafeSyncTest() {
        GlobalEventBus.init()
        for (i in 0..10) {
            var syncCount = 0
            val countDownLatch = CountDownLatch(5000)
            GlobalEventBus.subscribe<Int> {
                syncCount++
            }
            for (j in 0 until 5000) {
                Thread {
                    GlobalEventBus.postSyncEvent(1)
                    countDownLatch.countDown()
                }.start()
            }
            countDownLatch.await()
            Assert.assertEquals(5000, syncCount)
        }
    }

    @Test
    fun threadSafeAsyncTest() {
        GlobalEventBus.init()
        for (i in 0..10) {
            val asyncCount = AtomicInteger(0)
            val countDownLatch = CountDownLatch(5000)
            GlobalEventBus.subscribe<Int> {
                asyncCount.incrementAndGet()
            }
            for (j in 0 until 5000) {
                Thread {
                    GlobalEventBus.postEvent(1)
                    countDownLatch.countDown()
                }.start()
            }
            countDownLatch.await()
            Thread.sleep(100)
            Assert.assertEquals(5000, asyncCount.get())
        }
    }

    @Test
    fun lifeCycleTest() {

    }
}