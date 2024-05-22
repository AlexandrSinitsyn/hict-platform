package ru.itmo.hict.server.controller

import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.InvalidJwtException
import ru.itmo.hict.server.exception.ValidationException

@RestControllerAdvice
@CrossOrigin
class ApiExceptionController {
    @Autowired
    protected lateinit var requestUserInfo: RequestUserInfo

    protected fun <T> authorized(method: User.() -> T): T = requestUserInfo.user?.run(method)
        ?: throw ValidationException(DirectFieldBindingResult(this, this::class.java.simpleName).apply {
            reject("not-authorized", "You should be authorized to do this action")
        })

    protected fun <A : Any> A.response(): ResponseEntity<A> = ResponseEntity.ok(this)

    protected fun Any.success(): ResponseEntity<Boolean> = ResponseEntity.ok(true)


    @ExceptionHandler(ValidationException::class)
    fun validationException(validationException: ValidationException): ResponseEntity<Any> =
        validationException.bindingResult.allErrors.first().defaultMessage.run {
            ResponseEntity
                .badRequest()
                .headers(HttpHeaders().apply {
                    set("Content-Type", "${MediaType.TEXT_PLAIN_VALUE}; charset=utf-8")
                })
                .body(this)
        }

    @ExceptionHandler(JWTVerificationException::class, InvalidJwtException::class)
    fun jwtVerificationException(e: Exception): ResponseEntity<String> =
        ResponseEntity.badRequest().body("Invalid jwt: ${e.message}")
}