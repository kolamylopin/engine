package com.hatim.engine

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue
import com.hatim.engine.data.PricingRequest
import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.EventHandler
import com.lmax.disruptor.SleepingWaitStrategy
import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.dsl.ProducerType
import com.lmax.disruptor.util.DaemonThreadFactory
import org.junit.Test
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class Benchmarks {
    private val numberOfIterations = 10_000_000

    @Test
    fun testLmaxDisruptor() {
        DisruptorTest(numberOfIterations).test()
    }

    @Test
    fun testBlockingQueue() {
        BlockingQueueTest(numberOfIterations,
                ArrayBlockingQueue(numberOfIterations))
                .test()
    }

    @Test
    fun testDisruptorBlockingQueue() {
        BlockingQueueTest(numberOfIterations,
                DisruptorBlockingQueue(numberOfIterations))
                .test()
    }
}

class DisruptorTest(private val numberOfIterations: Int) {
    private var startTime = 0L
    private val testFinished = AtomicBoolean()

    fun test() {
        startTime = System.nanoTime()
        val bufferSize = 1024
        val disruptor = Disruptor<PricingRequest>(
                EventFactory<PricingRequest> { PricingRequest() },
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                SleepingWaitStrategy())
        disruptor.handleEventsWith(PricingHandler(startTime,
                numberOfIterations,
                testFinished))
        disruptor.start()
        val ringBuffer = disruptor.ringBuffer

        repeat(numberOfIterations) {
            val pricingRequestToPublish = PricingRequest(it.toString(), "destination", "msg")
            ringBuffer.publishEvent(
                    { event, _, arg -> event.copyInto(arg) },
                    pricingRequestToPublish)
        }

        while (!testFinished.get()) {
            TimeUnit.MILLISECONDS.sleep(1)
        }
    }

    private class PricingHandler(private val startTime: Long,
                                 private val numberOfIterations: Int,
                                 private val finished: AtomicBoolean)
        : EventHandler<PricingRequest> {
        override fun onEvent(event: PricingRequest?, sequence: Long, endOfBatch: Boolean) {
//        println("Received $sequence $event")
            if ((sequence.toInt() + 1) == numberOfIterations) {
                println("Took: ${(System.nanoTime() - startTime) / 1e6}")
                finished.set(true)
            }
        }
    }

}

class BlockingQueueTest(private val numberOfIterations: Int,
                        private val blockingQueue: BlockingQueue<PricingRequest>) {
    private var startTime = 0L
    private val testFinished = AtomicBoolean()

    fun test() {
        startTime = System.nanoTime()
        startConsumer()
        repeat(numberOfIterations) {
            blockingQueue.add(PricingRequest(it.toString(), "destination", "msg"))
        }

        while (!testFinished.get()) {
            TimeUnit.MILLISECONDS.sleep(1)
        }
    }

    private fun startConsumer() {
        var sequence = 0
        val runnable = Runnable {
            while (!testFinished.get()) {
                blockingQueue.take()
//                  println("Received $sequence $event")
                if (++sequence == numberOfIterations) {
                    println("Took: ${(System.nanoTime() - startTime) / 1e6}")
                    testFinished.set(true)
                }
            }
        }
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread.start()
    }
}