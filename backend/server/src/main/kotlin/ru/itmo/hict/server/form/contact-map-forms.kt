package ru.itmo.hict.server.form

import jakarta.validation.constraints.*
import java.util.*

class ContactMapCreationForm(
    @field:[NotNull org.hibernate.validator.constraints.UUID] val experimentId: UUID,
)

class ContactMapNameUpdateForm(
    @field:[NotNull NotBlank Size(min = 4, max = 256)] val name: String,
)

class ContactMapInfoUpdateForm(
    @field:[NotNull NotBlank Size(min = 4, max = 65536)] val description: String?,
    @field:[NotNull NotBlank Size(min = 4, max = 255)  ] val link: String?,
)
