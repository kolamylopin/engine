package com.hatim.engine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class EngineApplication

fun main(args: Array<String>) {
	runApplication<EngineApplication>(*args)
}
