package com.hatim.engine.services

import net.openhft.chronicle.core.Jvm
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Component
class OutputService(@Autowired private val executor: Executor,
                    @Autowired private val calculatorsService: CalculatorsService) {
    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(OutputService::class.java)
    }

    private val queueAppender = SingleChronicleQueueBuilder
            .single("D:\\queues").build()
            .acquireAppender()

    fun publish(message: Any) {
        executor.execute {
            calculatorsService.instance?.id?.let {
                val formattedMessage = "$it$$message"
                queueAppender.writeText(formattedMessage)
                logger.info("published $formattedMessage")
            }
        }
    }

    @PostConstruct
    fun init() {
        Executors.newSingleThreadExecutor().submit {
            var counter = 1
            while (true) {
                publish(counter++)
                Jvm.pause(1000)
            }
        }
    }
}
