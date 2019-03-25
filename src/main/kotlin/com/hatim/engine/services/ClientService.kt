package com.hatim.engine.services

import com.hatim.engine.data.PricingResponse
import com.hatim.engine.utils.Configuration
import net.openhft.chronicle.queue.ExcerptTailer
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import net.openhft.chronicle.wire.ReadMarshallable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PreDestroy

@Service
class ClientService(@Autowired private val configuration: Configuration) : ApplicationRunner {
    companion object {
        private val logger = LoggerFactory.getLogger(ClientService::class.java)
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val shuttingDown = AtomicBoolean(false)
//    private val pricingResponsesBuffer = PreallocatedRingBuffer(1024) { PricingResponse() }

    override fun run(args: ApplicationArguments?) {
//        pricingResponsesBuffer.initialize()
        executor.execute(this::startReader)
    }

    private fun startReader() {
        SingleChronicleQueueBuilder.single(configuration.inputQueue)
                .build().use { queue ->
                    queue.createTailer().apply {
                        toEnd()
                        val responseCreator = { PricingResponse() }
//                        val responseCreator = pricingResponsesBuffer::getNext
                        while (!shuttingDown.get()) {
                            lazilyReadDocument(responseCreator)?.let { response ->
                                when (response) {
                                    is PricingResponse -> processingPricingResponse(response)
                                }
                            }
                        }
                    }
                }
    }

    private fun processingPricingResponse(pricingResponse: PricingResponse) {
        val delay = (System.nanoTime() - pricingResponse.requestTimeStamp) / 1e6
        logger.info("Got response for ${pricingResponse.id} within $delay ms")
    }

    @PreDestroy
    fun destroy() {
        shuttingDown.set(true)
        executor.shutdown()
    }
}

fun ExcerptTailer.lazilyReadDocument(readerCreator: () -> ReadMarshallable): ReadMarshallable? {
    readingDocument().use { dc ->
        if (!dc.isPresent) {
            return null
        }
        readerCreator().run {
            readMarshallable(dc.wire()!!)
            return this
        }
    }
}
