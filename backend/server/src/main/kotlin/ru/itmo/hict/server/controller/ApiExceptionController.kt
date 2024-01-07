package ru.itmo.hict.server.controller

import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionController {
    @ExceptionHandler(JWTVerificationException::class)
    fun jwtVerificationException(e: JWTVerificationException): ResponseEntity<String> =
        ResponseEntity.badRequest().body("Invalid jwt: ${e.message}")
}