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
            ProcessBuilder("java -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -jar ${configuration.calculatorPath}".split(" "))

    private var calculatorInstances: List<InstanceInfo> = emptyList()
    private val instancesRetrievalCounter = AtomicInteger()
    private var lastLaunch = 0L
    private var lastCalculatorsNumber = 0

    val instance
        get(): InstanceInfo? {
            val availableCalculators = calculatorInstances.size
            if (availableCalculators != 0) {
                return calculatorInstances[instancesRetrievalCounter.getAndIncrement() % availableCalculators]
            }
            return null
        }

    val waitBetweenInstanceUpdates
        get() = configuration.secondsBetweenRegistryFetch

    init {
        processBuilder.directory(File(System.getProperty("user.home")))
    }

    override fun onEvent(event: EurekaEvent?) {
        val calculatorApplication: Application? = discoveryClient.getApplication(configuration.calculatorName)
        val calculatorsNumber = calculatorApplication?.size() ?: 0

        if (calculatorsNumber < configuration.calculatorsNumber
                && (calculatorsNumber != lastCalculatorsNumber
                        || System.currentTimeMillis() - lastLaunch > configuration.waitForCalculatorInMs)) {
            logger.info("Found only $calculatorsNumber calculators. Starting a new one")
            launchCalculator()
            lastLaunch = System.currentTimeMillis()
        }

        lastCalculatorsNumber = calculatorsNumber
        calculatorInstances = calculatorApplication?.instancesAsIsFromEureka ?: emptyList()
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