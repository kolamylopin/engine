package com.hatim.engine.services

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue
import com.hatim.engine.data.PricingRequest
import com.hatim.engine.utils.Configuration
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class PublisherService(@Autowired private val calculatorsService: CalculatorsService,
                       @Autowired private val configuration: Configuration) {
    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(PublisherService::class.java)
    }

    private val messagesQueue =
            DisruptorBlockingQueue<PricingRequest>(configuration.messagesQueueSize)

    private val queueAppender = SingleChronicleQueueBuilder
            .single(configuration.outputQueue).build()
            .acquireAppender()

    private val executorService = Executors.newFixedThreadPool(2)

    init {
        publishQueueMessages()
    }

    @PreDestroy
    fun destroy() {
        executorService.shutdownNow()
    }

    private fun publishQueueMessages() {
        executorService.execute {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val instance = calculatorsService.instance
                    if (instance != null) {
                        instance.id.let { destination ->
                            val message = messagesQueue.take()
                            message.destination = destination
                            queueAppender.writeDocument(message)
                            logger.info("published $message")
                        }
                    } else {
                        TimeUnit.SECONDS
                                .sleep(calculatorsService.waitBetweenInstanceUpdates)
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    fun addToQueue(message: PricingRequest) = messagesQueue.add(message)

    // For testing reasons only
    @PostConstruct
    fun addMessages() {
        executorService.execute {
            var counter = 1
            while (!Thread.currentThread().isInterrupted) {
                try {
                    counter++
                    addToQueue(PricingRequest(id = counter.toString(),
                            timestamp = System.nanoTime(),
                            message = "Message$counter"))
                    TimeUnit.MILLISECONDS.sleep(1000)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }
}
