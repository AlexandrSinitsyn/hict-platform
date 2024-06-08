package ru.itmo.hict.server

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.itmo.hict.server.logging.Logger

@TestConfiguration
class LoggerConfig {
    @Bean
    fun logger(): Logger = Logger("test")
}
