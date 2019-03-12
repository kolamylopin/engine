package com.hatim.engine.services

import com.hatim.engine.data.PricingRequest
import com.hatim.engine.utils.Configuration
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
                    @Autowired private val calculatorsService: CalculatorsService,
                    @Autowired private val configuration: Configuration) {
    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(OutputService::class.java)
    }

    private val queueAppender = SingleChronicleQueueBuilder
            .single(configuration.outputQueue).build()
            .acquireAppender()

    fun publish(message: PricingRequest) {
        executor.execute {
            calculatorsService.instance?.id?.let { destination ->
                message.destination = destination
                queueAppender.writeDocument(message)
                logger.info("published $message")
            }
        }
    }

    @PostConstruct
    fun init() {
        Executors.newSingleThreadExecutor().submit {
            var counter = 1
            while (true) {
                publish(PricingRequest(id = counter++.toString(), message = "Message$counter"))
                Jvm.pause(1000)
            }
        }
    }
}
