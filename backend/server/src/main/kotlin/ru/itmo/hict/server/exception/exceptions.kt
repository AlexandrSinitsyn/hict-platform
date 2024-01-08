package ru.itmo.hict.server.exception

import org.springframework.validation.BindingResult

class ValidationException(val bindingResult: BindingResult) : RuntimeException()

class InvalidAuthorizationTypeException(message: String) : RuntimeException(message)

class InvalidJwtException : RuntimeException("Invalid jwt token")

class InternalServerError(message: String) : Exception(message)
