package ru.itmo.hict.server.controller

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import okio.IOException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.itmo.hict.dto.FileInfoDto
import ru.itmo.hict.dto.FileInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.service.MinioService
import java.nio.file.Path
import kotlin.io.path.*

@RestController
@RequestMapping("/api/v1/files")
class FilesController(
    private val minioService: MinioService
) : ApiExceptionController() {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo
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

    private fun <T> authorized(method: User.() -> T): T = requestUserInfo.user?.run(method)
        ?: throw ValidationException(DirectFieldBindingResult(this, "experiment-controller").apply {
            reject("not-authorized", "You should be authorized to do this action")
        })

    @PostMapping("/publish")
    fun publish(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("type") fileType: FileType,
    ): ResponseEntity<FileInfoDto> = authorized {
        if (file.isEmpty) {
            throw ValidationException(DirectFieldBindingResult(this, "experiment-controller").apply {
                reject("empty-file", "File should not be empty")
            })
        }

        val filename = file.originalFilename ?: file.name
        try {
            val tempFile = createTempFile(tempDir, filename, ".tmp")
            file.transferTo(tempFile)

            val saved = minioService.load(fileType, filename, file.size, tempFile.inputStream())

            return@authorized ResponseEntity.ok(saved.file.toInfoDto())
        } catch (e: IOException) {
            throw IllegalStateException("Saving file failed: ${e.message}")
        }
    }
}
