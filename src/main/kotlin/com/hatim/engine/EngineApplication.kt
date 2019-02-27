package com.hatim.engine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EngineApplication

fun main(args: Array<String>) {
	runApplication<EngineApplication>(*args)
}
