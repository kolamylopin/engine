package com.hatim.engine.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class Configuration(
        @Value("\${application.calculator.name}") val calculatorName: String,
        @Value("\${application.calculator.path}") val calculatorPath: String,
        @Value("\${application.calculator.number}") val calculatorsNumber: Int,
        @Value("\${application.output-queue}") val outputQueue: String,
        @Value("\${application.messages-queue-size}") val messagesQueueSize: Int,
        @Value("\${eureka.client.registry-fetch-interval-seconds}") val secondsBetweenRegistryFetch: Long)
