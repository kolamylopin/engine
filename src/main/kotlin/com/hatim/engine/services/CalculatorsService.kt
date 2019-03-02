package com.hatim.engine.services

import com.netflix.discovery.EurekaClient
import com.netflix.discovery.EurekaEvent
import com.netflix.discovery.EurekaEventListener
import com.netflix.discovery.shared.Application
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@Service
class CalculatorsService(@Autowired val discoveryClient: EurekaClient,
                         private val calculatorsNbr: Int = 5) :
        EurekaEventListener, ApplicationRunner {
    companion object {
        private val logger = LoggerFactory.getLogger(CalculatorsService::class.java)
    }

    private var calculatorsCurrentNbr = 0
    private val initialCalculator = AtomicBoolean(true)
    private val processBuilder: ProcessBuilder = ProcessBuilder(
            "java -jar c:/Users/Hatim/IdeaProjects/jvm/build/libs/jvm-1.jar"
                    .split(" "))

    init {
        processBuilder.directory(File(System.getProperty("user.home")))
    }

    override fun onEvent(event: EurekaEvent?) {
        val calculatorInstances: Application? = discoveryClient.getApplication("dpCalculator")
        val updatedNbrOfCalculators = calculatorInstances?.size() ?: 0

        if (calculatorsCurrentNbr != updatedNbrOfCalculators || initialCalculator.get()) {
            calculatorsCurrentNbr = updatedNbrOfCalculators
            if (calculatorsCurrentNbr < calculatorsNbr) {
                logger.info("Found only $updatedNbrOfCalculators calculators. Starting a new one")
                launchCalculator()
                initialCalculator.compareAndSet(true, false)
            }
        }
    }

    override fun run(args: ApplicationArguments?) {
        discoveryClient.registerEventListener(this)
    }

    private fun launchCalculator() {
        try {
            processBuilder.start()
        } catch (e: Exception) {
            logger.error("Could not start the calculator", e)
        }
    }
}