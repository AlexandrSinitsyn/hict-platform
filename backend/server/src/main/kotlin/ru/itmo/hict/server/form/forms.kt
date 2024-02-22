package ru.itmo.hict.server.form

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class HiCMapCreationForm(
    @field:[NotNull NotBlank Size(min = 4, max = 256)  ] val name: String,
    @field:[NotNull NotBlank Size(min = 4, max = 65536)] val description: String,
)
