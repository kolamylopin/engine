package com.hatim.engine

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class EngineApplication : CommandLineRunner {
	companion object {
		private val logger = LoggerFactory.getLogger(EngineApplication::class.java)
	}

	override fun run(vararg args: String?) {
		logger.info("Application started")
	}
}

fun main(args: Array<String>) {
	runApplication<EngineApplication>(*args)
}
