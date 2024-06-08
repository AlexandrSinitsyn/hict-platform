package ru.itmo.hict.authorization.form

import jakarta.annotation.Nullable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import ru.itmo.hict.validator.NotBlankIfPresent

class RegisterForm(
    @field:[NotNull NotBlank Size(min = 3, max = 64)] val username: String,
    @field:[NotNull NotBlank Size(min = 3, max = 64)] val login: String,
    @field:[NotNull NotBlank Email                  ] val email: String,
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val password: String,
) {
    override fun toString() = "RegisterForm(username=$username, login=$login, email=$email, password=***)"
}

class EnterForm(
    @field:[Nullable NotBlankIfPresent Size(min = 3, max = 64)] val login: String?,
    @field:[Nullable NotBlankIfPresent Email                  ] val email: String?,
    @field:[NotNull  NotBlank          Size(min = 4, max = 32)] val password: String,
) {
    init {
        require(login != null || email != null) { "expected at least one non-null argument of [login, email]" }
    }

    override fun toString() = "EnterForm(login=$login, email=$email, password=***)"
}
