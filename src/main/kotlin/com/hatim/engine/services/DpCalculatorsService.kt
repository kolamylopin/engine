package com.hatim.engine.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class DpCalculatorsService {
    companion object {
        private val logger = LoggerFactory.getLogger(DpCalculatorsService::class.java)
    }

    @PostConstruct
    fun init() {
        logger.info("Starting dp calculators")
        repeat(5) {
            launchDpCalculator()
        }
        logger.info("finished dp calculators creation")
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