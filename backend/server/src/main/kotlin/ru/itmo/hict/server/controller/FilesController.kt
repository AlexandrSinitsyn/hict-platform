package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.validation.BindingResult
import ru.itmo.hict.server.form.ContactPersonForm
import ru.itmo.hict.server.form.ExperimentInfoUpdateForm
import ru.itmo.hict.server.form.ExperimentNameUpdateForm

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.itmo.hict.dto.ExperimentInfoDto
import ru.itmo.hict.dto.ExperimentInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.dto.UserInfoDto.Companion.toInfoDto
import ru.itmo.hict.entity.File
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.service.ExperimentService
import ru.itmo.hict.server.service.MinioService

@RestController
@RequestMapping("/api/v1/files")
class FilesController(
    private val minioService: MinioService
) : ApiExceptionController() {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    private fun <T> authorized(method: User.() -> T): T = requestUserInfo.user?.run(method)
        ?: throw ValidationException(DirectFieldBindingResult(this, "experiment-controller").apply {
            reject("not-authorized", "You should be authorized to do this action")
        })

    @PostMapping("/publish")
    fun publish(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("type") fileType: FileType,
        bindingResult: BindingResult,
    ): ResponseEntity<File> = authorized {
        if (file.isEmpty) {
            bindingResult.reject("empty-file", "File should not be empty")
        }

        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }

        return@authorized minioService.load(fileType, file.name, file.size, file.inputStream).run { ResponseEntity.ok(this.toInfoDto()) }
    }
}
