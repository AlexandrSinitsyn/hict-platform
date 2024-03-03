package ru.itmo.hict.server.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import ru.itmo.hict.server.validator.NotBlankIfPresent

class UpdateUserInfoForm(
    @field:[NotBlankIfPresent Size(min = 3, max = 100)] val username: String?,
    @field:[NotBlankIfPresent Size(min = 3, max = 100)] val login: String?,
    @field:[NotBlankIfPresent Email                   ] val email: String?,
)

class UpdatePasswordForm(
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val oldPassword: String,
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val newPassword: String,
)

class HiCMapCreationForm(
    @field:[NotNull NotBlank Size(min = 4, max = 256)  ] val name: String,
    @field:[NotNull NotBlank Size(min = 4, max = 65536)] val description: String,
)
