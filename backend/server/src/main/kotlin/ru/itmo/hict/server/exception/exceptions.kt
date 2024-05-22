package ru.itmo.hict.server.exception

import org.springframework.validation.BindingResult

class ValidationException(val bindingResult: BindingResult) : RuntimeException()

class NoSuchEntityException(fieldName: String, value: String) : RuntimeException("No such field $fieldName=`$value`")

class InvalidAuthorizationTypeException(message: String) : RuntimeException(message)

class InvalidJwtException : RuntimeException("Invalid jwt token")

class NoExperimentFoundException(experimentName: String)
    : RuntimeException("No experiment found with name=`$experimentName`")

open class LoadedFileException(message: String) : RuntimeException(message)
class EmptyLoadedFileException : LoadedFileException("File should not be empty")

class NoExperimentException(id: Long) : RuntimeException("Unknown experiment with id=$id")

class SameFieldException(fieldName: String, value: String)
    : RuntimeException("New $fieldName should be different `$value`")

class InternalServerError(message: String) : Exception(message)
