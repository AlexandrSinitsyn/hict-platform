package ru.itmo.hict.authorization

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import ru.itmo.hict.authorization.logging.Logger

@TestConfiguration
@Profile("!full-app-test")
class LoggerConfig() {
    @Bean
    fun logger(): Logger = Logger("test")
}
