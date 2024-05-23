package ru.itmo.hict.server.controller

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.validation.Valid
import okio.IOException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.itmo.hict.dto.FileInfoDto
import ru.itmo.hict.dto.FileInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.server.exception.EmptyLoadedFileException
import ru.itmo.hict.server.exception.InvalidFileTypeException
import ru.itmo.hict.server.exception.LoadingFailedException
import ru.itmo.hict.server.form.FileAttachmentForm
import ru.itmo.hict.server.service.ContactMapService
import ru.itmo.hict.server.service.ExperimentService
import ru.itmo.hict.server.service.FileService
import ru.itmo.hict.server.service.MinioService
import java.nio.file.Path
import kotlin.io.path.*

@RestController
@RequestMapping("/api/v1/files")
class FilesController(
    private val fileService: FileService,
    private val minioService: MinioService,
    private val experimentService: ExperimentService,
    private val contactMapService: ContactMapService,
) : ApiExceptionController() {
    private lateinit var tempDir: Path

    @PostConstruct
    fun init() {
        tempDir = createTempDirectory("hict_temp_${System.currentTimeMillis()}")
    }

    @OptIn(ExperimentalPathApi::class)
    @PreDestroy
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    @PostMapping("/publish")
    fun publish(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("type") fileType: FileType,
    ): ResponseEntity<FileInfoDto> = authorized {
        if (file.isEmpty) {
            throw EmptyLoadedFileException()
        }

        val filename = file.originalFilename ?: file.name
        try {
            val tempFile = createTempFile(tempDir, filename, ".tmp")
            file.transferTo(tempFile)

            val saved = fileService.save(fileType, filename, file.size)

            minioService.upload(fileType, "${saved.file.id}", file.size, tempFile.inputStream())

            saved.file
        } catch (e: IOException) {
            throw LoadingFailedException(e.message ?: "I/O exception")
        }
    }.toInfoDto().response()

    @PostMapping("/attach/experiment/{experimentId}")
    fun attachToExperiment(
        @PathVariable("experimentId") experimentId: Long,
        @RequestBody @Valid fileAttachmentForm: FileAttachmentForm,
    ): ResponseEntity<Boolean> = authorized {
        if (fileAttachmentForm.fileType != FileType.FASTA) {
            throw InvalidFileTypeException(fileAttachmentForm.fileType)
        }

        experimentService.attachToExperiment(experimentId, fileAttachmentForm.fileId)
    }.success()

    @PostMapping("/attach/contact-map/{contactMapId}")
    fun attachToContactMap(
        @PathVariable("contactMapId") contactMapId: Long,
        @RequestBody @Valid fileAttachmentForm: FileAttachmentForm,
    ): ResponseEntity<Boolean> = authorized {
        contactMapService.attachToContactMap(contactMapId, fileAttachmentForm.fileId, fileAttachmentForm.fileType)
    }.success()
}
