package ru.itmo.hict.server.controller

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import okio.IOException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.itmo.hict.dto.FileInfoDto
import ru.itmo.hict.dto.FileInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.server.exception.EmptyLoadedFileException
import ru.itmo.hict.server.exception.LoadedFileException
import ru.itmo.hict.server.service.MinioService
import java.nio.file.Path
import kotlin.io.path.*

@RestController
@RequestMapping("/api/v1/files")
class FilesController(
    private val minioService: MinioService
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

            minioService.load(fileType, filename, file.size, tempFile.inputStream())
                .file
        } catch (e: IOException) {
            throw LoadedFileException("Saving file failed: ${e.message}")
        }
    }.toInfoDto().response()
}
