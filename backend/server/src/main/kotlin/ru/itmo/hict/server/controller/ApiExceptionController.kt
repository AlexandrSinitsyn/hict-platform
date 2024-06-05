package ru.itmo.hict.server.controller

import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ClientException
import ru.itmo.hict.server.exception.UnauthorizedException
import ru.itmo.hict.server.exception.ValidationException

@RestControllerAdvice
@CrossOrigin
class ApiExceptionController {
    @Autowired
    protected lateinit var requestUserInfo: RequestUserInfo

    protected fun <T> authorized(method: User.() -> T): T = requestUserInfo.user?.run(method)
        ?: throw UnauthorizedException()

    protected fun <A : Any> A.response(): ResponseEntity<A> = ResponseEntity.ok(this)

    protected fun Any.success(): ResponseEntity<Boolean> = ResponseEntity.ok(true)

    private fun error(message: String) = ResponseEntity.badRequest()
        .headers(HttpHeaders().apply {
            set("Content-Type", "${MediaType.TEXT_PLAIN_VALUE}; charset=utf-8")
        })
        .body(message)

    @ExceptionHandler(ValidationException::class)
    fun validationException(validationException: ValidationException): ResponseEntity<String> =
        error(validationException.bindingResult.allErrors.first().defaultMessage ?: "?")

    @ExceptionHandler(JWTVerificationException::class)
    fun jwtVerificationException(jwtException: JWTVerificationException): ResponseEntity<String> =
        error("Invalid jwt: ${jwtException.message}")

    @ExceptionHandler(ClientException::class)
    fun clientException(clientException: ClientException): ResponseEntity<String> =
        error(clientException.message)
}