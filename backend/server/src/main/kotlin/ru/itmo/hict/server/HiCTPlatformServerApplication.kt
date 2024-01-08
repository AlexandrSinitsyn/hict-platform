package ru.itmo.hict.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableJpaRepositories
@SpringBootApplication
class HiCTPlatformServerApplication

@EntityScan("ru.itmo.hict.entity")
@ComponentScan("ru.itmo.hict.dto", "ru.itmo.hict.server")
@Configuration
class HiCTPlatformServerConfig

fun main(args: Array<String>) {
	runApplication<HiCTPlatformServerApplication>(*args)
}
