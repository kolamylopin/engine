package com.hatim.engine.services

import com.hatim.engine.utils.Configuration
import com.netflix.appinfo.InstanceInfo
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
import java.util.concurrent.atomic.AtomicInteger

@Service
class CalculatorsService(@Autowired val discoveryClient: EurekaClient,
                         @Autowired val configuration: Configuration) :
        EurekaEventListener, ApplicationRunner {
    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CalculatorsService::class.java)
    }

    private val processBuilder =
            ProcessBuilder("java -jar ${configuration.calculatorPath}".split(" "))

    private var calculatorsCurrentNbr = 0
    private val initialCalculator = AtomicBoolean(true)
    private var calculatorInstances: List<InstanceInfo> = emptyList()
    private val instancesRetrievalCounter = AtomicInteger()

    val instance
        get(): InstanceInfo? {
            val availableCalculators = calculatorInstances.size
            if (availableCalculators != 0) {
                return calculatorInstances[instancesRetrievalCounter.getAndIncrement() % availableCalculators]
            }
            return null
        }

    init {
        processBuilder.directory(File(System.getProperty("user.home")))
    }

    override fun onEvent(event: EurekaEvent?) {
        val dpCalculatorApplication: Application? = discoveryClient.getApplication(configuration.calculatorName)
        val updatedNbrOfCalculators = dpCalculatorApplication?.size() ?: 0

        if (calculatorsCurrentNbr != updatedNbrOfCalculators || initialCalculator.get()) {
            calculatorsCurrentNbr = updatedNbrOfCalculators
            if (calculatorsCurrentNbr < configuration.calculatorsNumber) {
                logger.info("Found only $updatedNbrOfCalculators calculators. Starting a new one")
                launchCalculator()
                initialCalculator.compareAndSet(true, false)
            }
        } else {
            calculatorInstances = dpCalculatorApplication?.instancesAsIsFromEureka ?: emptyList()
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