package ru.itmo.hict.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories
@SpringBootApplication
class AuthorizationApplication

@EntityScan("ru.itmo.hict.entity")
@Configuration
class ApplicationConfig

fun main(args: Array<String>) {
	runApplication<AuthorizationApplication>(*args)
}
