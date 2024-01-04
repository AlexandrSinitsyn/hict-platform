package ru.itmo.hict.authorization.exceptions

import org.springframework.validation.BindingResult

class ValidationException(val bindingResult: BindingResult) : RuntimeException()
