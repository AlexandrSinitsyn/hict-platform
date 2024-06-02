package ru.itmo.hict.server.form

import jakarta.validation.constraints.*
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.validator.EnumValues
import java.util.UUID

class FileAttachmentForm(
    @field:[NotNull                                    ] val fileId: UUID,
    @field:[NotNull EnumValues(clazz = FileType::class)] val fileType: FileType,
)
