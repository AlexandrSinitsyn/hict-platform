package ru.itmo.hict.authorization.controller

import com.auth0.jwt.exceptions.JWTCreationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.hict.authorization.exceptions.ValidationException

@RestControllerAdvice
class ApiExceptionController {
    @ExceptionHandler(ValidationException::class)
    fun validationException(validationException: ValidationException): ResponseEntity<Any> =
        validationException.bindingResult.allErrors.first().defaultMessage.run { ResponseEntity.badRequest().body(this) }

    @ExceptionHandler(JWTCreationException::class)
    fun jwtCreationException(jwtCreationException: JWTCreationException): ResponseEntity<Any> =
        ResponseEntity.badRequest().body(jwtCreationException.message ?: "JWT can not be created. Retry")
}
