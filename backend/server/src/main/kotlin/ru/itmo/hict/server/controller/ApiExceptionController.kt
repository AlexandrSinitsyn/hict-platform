package ru.itmo.hict.server.controller

import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.hict.server.exception.InvalidJwtException
import ru.itmo.hict.server.exception.ValidationException

@RestControllerAdvice
class ApiExceptionController {
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