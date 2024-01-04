package ru.itmo.hict.authorization.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.hict.authorization.exceptions.ValidationException

@RestControllerAdvice
class ApiExceptionController {
    @ExceptionHandler(ValidationException::class)
    fun validationException(validationException: ValidationException): ResponseEntity<Any> =
        validationException.bindingResult.allErrors.first().defaultMessage.run { ResponseEntity.badRequest().body(this) }
}
