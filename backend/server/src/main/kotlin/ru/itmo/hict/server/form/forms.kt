package ru.itmo.hict.server.form

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class HiCMapCreationForm(
    @NotNull @NotBlank @Size(min = 4, max = 256) val name: String,
    @NotNull @NotBlank val description: String,
)
