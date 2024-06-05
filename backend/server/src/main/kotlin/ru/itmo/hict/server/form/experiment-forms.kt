package ru.itmo.hict.server.form

import jakarta.validation.constraints.*

class ExperimentNameUpdateForm(
    @field:[NotNull NotBlank Size(min = 4, max = 256)] val name: String,
)

class ExperimentInfoUpdateForm(
    @field:[NotNull NotBlank Size(min = 4, max = 65536)] val description: String?,
    @field:[NotNull NotBlank Size(min = 4, max = 255)  ] val paper: String?,
    @field:[NotNull NotBlank Size(min = 4, max = 255)  ] val acknowledgement: String?,
)

class ContactPersonForm(
    @field:[NotNull NotBlank Size(min = 4, max = 100)      ] val name: String,
    @field:[NotNull NotBlank Email Size(min = 4, max = 100)] val email: String,
)
