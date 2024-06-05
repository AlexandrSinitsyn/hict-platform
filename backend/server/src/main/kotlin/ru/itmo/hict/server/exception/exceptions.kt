package ru.itmo.hict.server.exception

import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import ru.itmo.hict.dto.FileType
import java.util.UUID

open class InternalServerError(message: String) : Exception(message) {
    constructor() : this("")
}

open class ClientException(override val message: String) : RuntimeException(message) {
    constructor() : this("")
}

open class ValidationException private constructor() : ClientException() {
    private sealed interface Either<out L, out R> {
        class Left<T>(private val value: T) : Either<T, Nothing> {
            override fun isLeft() = true
            override fun left() = value
        }
        class Right<T>(private val value: T) : Either<Nothing, T> {
            override fun isLeft() = false
            override fun right() = value
        }

        private fun illegalState() = IllegalStateException("Illegal Either state")

        fun isLeft(): Boolean = throw illegalState()
        fun left(): L = throw illegalState()
        fun right(): R = throw illegalState()
    }

    private lateinit var _value: Either<BindingResult, Triple<String, String, String>>

    val bindingResult: BindingResult
        get() = when {
            _value.isLeft() -> _value.left()
            else -> _value.right().let { (target, errorCode, message) ->
                DirectFieldBindingResult(this, target).apply {
                    reject(errorCode, message)
                }
            }
        }

    constructor(bindingResult: BindingResult) : this() {
        _value = Either.Left(bindingResult)
    }

    constructor(target: String, errorCode: String, message: String) : this() {
        _value = Either.Right(Triple(target, errorCode, message))
    }

    companion object {
        fun BindingResult.alert() = when {
            hasErrors() -> throw ValidationException(this)
            else -> Unit
        }
    }
}

class InvalidJwtException : ValidationException("jwt", "invalid-jwt", "Invalid JWT token passed")

class InvalidAuthorizationTypeException(type: String)
    : ValidationException("auth", "invalid-auth-type", "Expected Bearer but was `$type`")

class UnauthorizedException : ClientException("You should be authorized for this")

class NotConfirmedException
    : ValidationException("auth", "invalid-password", "You must confirm this action with a password")

open class NoSuchEntityException(entityName: String, fieldName: String, value: String)
    : ClientException("No such $entityName with $fieldName=`$value`")

class NoExperimentFoundException private constructor(field: String, value: String)
    : NoSuchEntityException("experiment", field, value) {
    constructor(experimentName: String) : this("name", experimentName)
    constructor(experimentId: UUID) : this("id", "$experimentId")
}

class NoContactMapFoundException private constructor(field: String, value: String)
    : NoSuchEntityException("contact-map", field, value) {
    constructor(contactMapName: String) : this("name", contactMapName)
    constructor(contactMapId: UUID) : this("id", "$contactMapId")
}

open class SameFieldException(fieldName: String, value: String)
    : ClientException("New $fieldName should be different: `$value`")

class SamePasswordException : SameFieldException("password", "***")

open class LoadedFileException(message: String) : ClientException(message)

class EmptyLoadedFileException : LoadedFileException("File should not be empty")

class InvalidFileTypeException(fileType: FileType)
    : LoadedFileException("Uploaded file with unexpected type `$fileType`")

open class LoadingFailedException(message: String) : LoadedFileException("Error while loading file: $message")

class InvalidFileSessionException(session: UUID, partIndex: Long? = null)
    : LoadingFailedException("session `$session` is invalid or has already expired"
        + (partIndex?.let { "when part $it was loading" } ?: ""))
