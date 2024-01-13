package ru.itmo.hict.authorization.logging

import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private object FakeException : RuntimeException("fake")

open class Logger(name: String) {
    private val logger = LoggerFactory.getLogger(name)

    private val here: String
        get() = FakeException.stackTrace[3].run {
            "%s.%s(%s:%s)".format(
                className,
                methodName,
                fileName,
                lineNumber,
            )
        }

    private class RequestType(val httpRequestType: String, val path: String) {
        override fun toString(): String = "$httpRequestType($path)"
    }

    private fun process(
        level: Level,
        here: String?,
        request: RequestType?,
        process: String,
        stage: String,
        message: String,
    ) = logger.atLevel(level).log { "${here ?: ""} -- ${request ?: ""} -- $process($stage)> $message" }

    fun syslog(process: String, message: String) =
        process(Level.INFO, here, null, "syslog", process, message)

    fun request(httpRequestType: String, path: String, process: String, message: String) =
        process(Level.INFO, null, RequestType(httpRequestType, path), "request", process, message)

    fun infoHere(process: String, stage: String, message: String) =
        process(Level.INFO, here, null, process, stage, message)
    fun warnHere(process: String, stage: String, message: String) =
        process(Level.WARN, here, null, process, stage, message)
    fun errorHere(process: String, stage: String, message: String) =
        process(Level.ERROR, here, null, process, stage, message)

    fun info(process: String, stage: String, message: String) =
        process(Level.INFO, null, null, process, stage, message)
    fun warn(process: String, stage: String, message: String) =
        process(Level.WARN, null, null, process, stage, message)
    fun error(process: String, stage: String, message: String) =
        process(Level.ERROR, null, null, process, stage, message)
}

@Configuration
class LoggerConfig {
    @Bean
    fun logger(): Logger = Logger("authorization")
}
