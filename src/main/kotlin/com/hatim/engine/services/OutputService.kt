package com.hatim.engine.services

import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.Executor

@Component
class OutputService(@Autowired private val executor: Executor) {
    companion object {
        private val logger = LoggerFactory.getLogger(OutputService::class.java)
    }

    private val queueAppender = SingleChronicleQueueBuilder
            .single("D:\\queues").build()
            .acquireAppender()

    fun publish(message: Any) {
        executor.execute {
            queueAppender.writeText(message.toString())
            logger.info("published $message")
        }
    }
}
