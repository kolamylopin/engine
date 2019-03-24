package com.hatim.engine.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
final data class Configuration(
        @Value("\${application.calculator.name}") val calculatorName: String,
        @Value("\${application.calculator.path}") val calculatorPath: String,
        @Value("\${application.calculator.args}") val calculatorArgs: String,
        @Value("\${application.calculator.number}") val calculatorsNumber: Int,
        @Value("\${application.input-queue}") val inputQueue: String,
        @Value("\${application.output-queue}") val outputQueue: String,
        @Value("\${application.messages-queue-size}") val messagesQueueSize: Int,
        @Value("\${eureka.client.registry-fetch-interval-seconds}") val secondsBetweenRegistryFetch: Long,
        @Value("\${application.wait-for-calculator-in-sec}") private val waitForCalculatorInSec: Long) {

    val waitForCalculatorInMs = 1000 * waitForCalculatorInSec
}
