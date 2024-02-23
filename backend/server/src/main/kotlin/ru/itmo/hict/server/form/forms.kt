package ru.itmo.hict.server.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class UpdateUsernameForm(
    @field:[NotNull NotBlank Size(min = 3, max = 100)] val username: String,
)

class UpdateLoginForm(
    @field:[NotNull NotBlank Size(min = 3, max = 100)] val login: String,
)

class UpdateEmailForm(
    @field:[NotNull NotBlank Email] val email: String,
)

class UpdatePasswordForm(
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val oldPassword: String,
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val newPassword: String,
)

class HiCMapCreationForm(
    @field:[NotNull NotBlank Size(min = 4, max = 256)  ] val name: String,
    @field:[NotNull NotBlank Size(min = 4, max = 65536)] val description: String,
)
