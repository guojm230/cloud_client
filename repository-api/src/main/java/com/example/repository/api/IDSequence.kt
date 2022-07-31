package com.example.repository.api

import java.util.concurrent.atomic.AtomicInteger

private var id = AtomicInteger(0)
private val lock = Any()

fun nextID():Int{
    return id.incrementAndGet()
}