package com.hatim.engine.data

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class PreallocatedRingBuffer<T>(val capacity: Int,
                                private val initializer: () -> T) {
    private val data = arrayOfNulls<Any?>(capacity)
    private val next = AtomicInteger()

    private val initialized = AtomicBoolean(false)

    fun initialize() {
        if (initialized.compareAndSet(false, true)) {
            for (i in 0 until capacity) {
                data[i] = initializer()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getNext(): T = data[next.getAndIncrement() % capacity] as T
}