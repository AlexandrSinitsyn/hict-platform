package ru.itmo.hict.server.exception

import org.springframework.validation.BindingResult

class ValidationException(val bindingResult: BindingResult) : RuntimeException()

class NoSuchEntityException(fieldName: String, value: String) : RuntimeException("No such field $fieldName=`$value`")

class InvalidAuthorizationTypeException(message: String) : RuntimeException(message)

class InvalidJwtException : RuntimeException("Invalid jwt token")

class InternalServerError(message: String) : Exception(message)
