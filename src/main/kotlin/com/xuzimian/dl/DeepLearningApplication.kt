package com.xuzimian.dl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class DeepLearningApplication

fun main(args: Array<String>) {
	runApplication<DeepLearningApplication>(*args)
}
