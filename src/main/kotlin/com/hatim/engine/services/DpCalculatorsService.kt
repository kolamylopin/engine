package com.hatim.engine.services

import com.netflix.discovery.EurekaClient
import com.netflix.discovery.EurekaEvent
import com.netflix.discovery.EurekaEventListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class DpCalculatorsService(@Autowired val discoveryClient: EurekaClient,
                           private val calculatorsNbr: Int = 5,
                           private val waitBeforeCreatingCalculator:Int = 15):
        EurekaEventListener, ApplicationRunner {
    companion object {
        private val logger = LoggerFactory.getLogger(DpCalculatorsService::class.java)
    }

    private val attempts = AtomicInteger()

    override fun onEvent(event: EurekaEvent?) {
        val engineInstances = discoveryClient.getApplication("dpCalculator")
        if ((engineInstances == null || engineInstances.size() < calculatorsNbr)
                && attempts.getAndIncrement() >= waitBeforeCreatingCalculator){
            val currentNbrOfCalculators = engineInstances?.size() ?: 0
            val calculatorsToStart = calculatorsNbr - currentNbrOfCalculators
            logger.info("Found only $currentNbrOfCalculators calculators, Starting $calculatorsToStart")
            repeat(calculatorsToStart) { launchDpCalculator() }
            attempts.set(0)
        }
    }

    override fun run(args: ApplicationArguments?) {
        discoveryClient.registerEventListener(this)
    }

    private fun launchDpCalculator() {
        try {
            ProcessBuilder(
                    "java -jar c:/Users/Hatim/IdeaProjects/jvm/build/libs/jvm-1.jar"
                            .split(" ")).start()
        } catch (e: Exception) {
            logger.error("Could not start the dp calculator", e)
        }
    }
}