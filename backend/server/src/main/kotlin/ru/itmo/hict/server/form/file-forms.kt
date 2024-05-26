package ru.itmo.hict.server.form

import jakarta.validation.constraints.*
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.validator.EnumValues
import java.util.UUID

class FileAttachmentForm(
    @field:[NotNull NotBlank                                     Size(min = 3, max = 100)] val fileId: UUID,
    @field:[NotNull NotBlank EnumValues(clazz = FileType::class) Size(min = 3, max = 100)] val fileType: FileType,
)
