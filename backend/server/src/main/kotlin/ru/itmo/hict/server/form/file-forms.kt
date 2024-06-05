package ru.itmo.hict.server.form

import jakarta.validation.constraints.*
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.validator.EnumValues
import java.util.UUID

class FileUploadingStreamForm(
    @field:[NotNull EnumValues(clazz = FileType::class)] val type: FileType,
    @field:[NotNull NotBlank Size(min = 3, max = 100)  ] val filename: String,
    @field:[NotNull Positive                           ] val fileSize: Long,
)

class FileAttachmentForm(
    @field:[NotNull                                    ] val fileId: UUID,
    @field:[NotNull EnumValues(clazz = FileType::class)] val fileType: FileType,
)
