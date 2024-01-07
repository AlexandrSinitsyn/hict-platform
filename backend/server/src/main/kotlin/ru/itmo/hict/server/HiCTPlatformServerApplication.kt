package ru.itmo.hict.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HiCTPlatformServerApplication

fun main(args: Array<String>) {
	runApplication<HiCTPlatformServerApplication>(*args)
}
