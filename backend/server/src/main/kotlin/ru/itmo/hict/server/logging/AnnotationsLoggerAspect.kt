package ru.itmo.hict.server.logging

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Info(val process: String, val message: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Warn(val process: String, val message: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Error(val process: String, val message: String)

@Aspect
@Component
class AnnotationsLoggerAspect(
    private val logger: Logger,
) {
    @Before("@annotation(info)")
    fun info(joinPoint: JoinPoint, info: Info) = logger.info(info.process, joinPoint.signature.name, info.message)

    @Before("@annotation(warn)")
    fun warn(joinPoint: JoinPoint, warn: Warn) = logger.info(warn.process, joinPoint.signature.name, warn.message)

    @Before("@annotation(error)")
    fun error(joinPoint: JoinPoint, error: Error) = logger.info(error.process, joinPoint.signature.name, error.message)
}
