package ru.itmo.hict.server.form

import jakarta.validation.constraints.*
import ru.itmo.hict.validator.NotBlankIfPresent

class UpdateUserInfoForm(
    @field:[NotBlankIfPresent Size(min = 3, max = 100)] val username: String?,
    @field:[NotBlankIfPresent Size(min = 3, max = 100)] val login: String?,
    @field:[NotBlankIfPresent Email                   ] val email: String?,
)

class UpdatePasswordForm(
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val oldPassword: String,
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val newPassword: String,
)

class GroupCreationForm(
    @field:[NotNull NotBlank Size(min = 3, max = 100)] val name: String,
)

class GroupUpdateNameForm(
    @field:[NotNull NotBlank Size(min = 3, max = 100)] val name: String,
)
