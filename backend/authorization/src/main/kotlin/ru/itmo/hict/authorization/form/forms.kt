package ru.itmo.hict.authorization.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class RegisterForm(
    @NotNull @NotBlank @Size(min = 3, max = 64) val username: String,
    @NotNull @NotBlank @Size(min = 3, max = 64) val login: String,
    @NotNull @NotBlank @Email val email: String,
    @NotNull @NotBlank @Size(min = 4, max = 128) val passwordSha: String,
) {
    override fun toString() = "RegisterForm(username=$username, login=$login, email=$email, password=***)"
}

class EnterForm(
    @Size(min = 3, max = 64) val login: String?,
    @Email val email: String?,
    @NotNull @NotBlank @Size(min = 4, max = 128) val passwordSha: String,
) {
    override fun toString() =
        (login?.let { "login=$it, " } ?: "") + (email?.let { "email=$it, " } ?: "") + "password=***"
}
