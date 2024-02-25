package ru.itmo.hict.authorization.exception

import org.springframework.validation.BindingResult

class ValidationException(val bindingResult: BindingResult) : RuntimeException()
