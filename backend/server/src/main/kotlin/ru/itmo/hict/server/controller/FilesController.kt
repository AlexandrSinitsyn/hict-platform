package ru.itmo.hict.server.controller

import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import okio.IOException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.itmo.hict.dto.FileInfoDto
import ru.itmo.hict.dto.FileInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.server.exception.EmptyLoadedFileException
import ru.itmo.hict.server.exception.InvalidFileTypeException
import ru.itmo.hict.server.exception.InvalidFileSessionException
import ru.itmo.hict.server.exception.LoadingFailedException
import ru.itmo.hict.server.form.FileAttachmentForm
import ru.itmo.hict.server.form.FileUploadingStreamForm
import ru.itmo.hict.server.logging.Logger
import ru.itmo.hict.server.service.ContactMapService
import ru.itmo.hict.server.service.ExperimentService
import ru.itmo.hict.server.service.FileService
import ru.itmo.hict.server.service.MinioService
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.io.path.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin
class FilesController(
    private val logger: Logger,
    private val fileService: FileService,
    private val minioService: MinioService,
    private val experimentService: ExperimentService,
    private val contactMapService: ContactMapService,
    @Value("\${SERVER_LOCAL_STORAGE_PATH}") private val storagePath: String,
) : ApiExceptionController() {
    private val sessions: MutableMap<UUID, Instant> = hashMapOf()
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private fun extendLifetime(session: UUID) =
        sessions.put(session, Instant.now() + 1.toDuration(DurationUnit.MINUTES).toJavaDuration())

    @OptIn(ExperimentalPathApi::class)
    private fun delayCleaning(session: UUID) {
        scheduler.schedule({
            sessions[session]?.let {
                if (it.isBefore(Instant.now())) {
                    logger.info("cleanup", "$session", "deleting")
                    session.tmpDir().deleteRecursively()
                    sessions -= session
                } else {
                    logger.info("cleanup", "$session", "delay")
                    delayCleaning(session)
                }
            }
        }, 2, TimeUnit.MINUTES)
    }

    @PostConstruct
    fun init() {
        Path(storagePath).createDirectories()
    }

    private fun UUID.tmpDir() = Path(storagePath, this.toString())

    @GetMapping("/session/init")
    fun initSession(): ResponseEntity<UUID> =
        UUID.randomUUID().also {
            it.tmpDir().createDirectories()
            extendLifetime(it)
            delayCleaning(it)
            logger.info("session-init", "$it", "schedule setup")
        }.response()

    @PostMapping("/session/{session}/close")
    fun closeSession(
        @PathVariable("session") session: UUID,
        @RequestBody @Valid form: FileUploadingStreamForm,
    ): ResponseEntity<FileInfoDto> = authorized {
        if (session !in sessions) {
            throw InvalidFileSessionException(session)
        }

        try {
            val saved = fileService.save(form.type, form.filename, form.fileSize)

            logger.info("uploading", "$session", "file info saved")

            val filename = "${saved.file.id!!}.${form.type.extension}"

            val file = Path(storagePath, filename)

            session.tmpDir().listDirectoryEntries().sortedBy { it.name.toLong() }.map { it.toFile().readBytes() }
                .forEach { file.run { if (!exists()) writeBytes(it) else appendBytes(it) } }

            logger.info("uploading", "$session", filename)

            minioService.upload(form.type, filename, form.fileSize, file.toFile().inputStream())

            sessions[session] = Instant.MIN

            saved.file
        } catch (e: IOException) {
            throw LoadingFailedException(e.message ?: "I/O exception")
        }
    }.toInfoDto().response()

    @PostMapping("/publish")
    fun publish(
        @RequestPart("session") session: UUID,
        @RequestPart("partIndex") partIndex: Long,
        @RequestPart("file") file: MultipartFile,
    ): ResponseEntity<Boolean> = authorized {
        when {
            file.isEmpty -> throw EmptyLoadedFileException()
            session !in sessions -> throw InvalidFileSessionException(session, partIndex)
        }

        try {
            val localFile = session.tmpDir().resolve("$partIndex")
            file.transferTo(localFile)
            logger.info("uploading", "$session", "arrived: $partIndex")
        } catch (e: IOException) {
            throw LoadingFailedException(e.message ?: "I/O exception")
        }
    }.success()

    @PostMapping("/attach/experiment/{experimentId}")
    fun attachToExperiment(
        @PathVariable("experimentId") experimentId: UUID,
        @RequestBody @Valid fileAttachmentForm: FileAttachmentForm,
    ): ResponseEntity<Boolean> = authorized {
        if (fileAttachmentForm.fileType != FileType.FASTA) {
            throw InvalidFileTypeException(fileAttachmentForm.fileType)
        }

        experimentService.attachToExperiment(experimentId, fileAttachmentForm.fileId)
        logger.info("attached-experiment", experimentId.toString(), fileAttachmentForm.fileId.toString())
    }.success()

    @PostMapping("/attach/contact-map/{contactMapId}")
    fun attachToContactMap(
        @PathVariable("contactMapId") contactMapId: UUID,
        @RequestBody @Valid fileAttachmentForm: FileAttachmentForm,
    ): ResponseEntity<Boolean> = authorized {
        contactMapService.attachToContactMap(contactMapId, fileAttachmentForm.fileId, fileAttachmentForm.fileType)
        logger.info("attached-contact-map", contactMapId.toString(), fileAttachmentForm.fileId.toString())
    }.success()
}
