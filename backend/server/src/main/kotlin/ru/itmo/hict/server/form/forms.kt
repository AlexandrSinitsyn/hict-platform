package ru.itmo.hict.server.form

import jakarta.validation.constraints.*
import ru.itmo.hict.entity.Role
import ru.itmo.hict.server.validator.NotBlankIfPresent

class UpdateUserInfoForm(
    @field:[NotBlankIfPresent Size(min = 3, max = 100)] val username: String?,
    @field:[NotBlankIfPresent Size(min = 3, max = 100)] val login: String?,
    @field:[NotBlankIfPresent Email                   ] val email: String?,
)

class UpdateRoleForm(
    @field:[NotNull PositiveOrZero] val id: Long,
    @field:[NotNull               ] val newRole: Role,
)

class UpdatePasswordForm(
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val oldPassword: String,
    @field:[NotNull NotBlank Size(min = 4, max = 32)] val newPassword: String,
)

class HiCMapCreationForm(
    @field:[NotNull NotBlank Size(min = 4, max = 256)  ] val name: String,
    @field:[NotNull NotBlank Size(min = 4, max = 65536)] val description: String,
)
