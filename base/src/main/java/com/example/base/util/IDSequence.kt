package com.example.base.util

import java.util.concurrent.atomic.AtomicInteger

private var id = AtomicInteger(0)

fun nextID():Int{
    return id.incrementAndGet()
}