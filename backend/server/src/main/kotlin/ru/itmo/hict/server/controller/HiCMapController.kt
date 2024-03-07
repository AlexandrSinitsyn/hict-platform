package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.itmo.hict.dto.HiCMapInfoDto
import ru.itmo.hict.dto.HiCMapInfoDto.Companion.toInfoDto
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.HiCMapCreationForm
import ru.itmo.hict.server.service.HiCMapService
import ru.itmo.hict.server.service.MinioService
import ru.itmo.hict.server.validator.HiCMapCreationFormValidator

@RestController
@RequestMapping("/api/v1/hi-c")
class HiCMapController(
    private val hiCMapService: HiCMapService,
    private val hiCMapCreationFormValidator: HiCMapCreationFormValidator,
) : ApiExceptionController() {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    @InitBinder("hiCMapCreationForm")
    fun initPublishBinder(webDataBinder: WebDataBinder) {
        webDataBinder.addValidators(hiCMapCreationFormValidator)
    }

    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<HiCMapInfoDto>> =
        hiCMapService.getAll().map { it.toInfoDto() }.run { ResponseEntity.ok(this) }

    @GetMapping("/acquire/{id}")
    fun getById(@PathVariable("id") id: Long): ResponseEntity<HiCMapInfoDto> =
        hiCMapService.getById(id)?.run {
            hiCMapService.view(id)
            ResponseEntity.ok(this.toInfoDto())
        } ?: ResponseEntity.notFound().build()

    @PostMapping("/publish")
    fun publish(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("form") @Valid hiCMapCreationForm: HiCMapCreationForm,
        bindingResult: BindingResult,
    ): ResponseEntity<HiCMapInfoDto> {
        // fixme somehow for @RequestBody validator automatically works, but not for the @RequestPart
        hiCMapCreationFormValidator.validate(hiCMapCreationForm, bindingResult)

        val user = requestUserInfo.user

        when {
            user == null -> bindingResult.reject("not-authorized",
                "You must be authorized for publishing a Hi-C map")
            file.isEmpty -> bindingResult.reject("empty-file", "File should not be empty")
        }

        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }

        return hiCMapService.load(user!!, hiCMapCreationForm.name, hiCMapCreationForm.description,
            MinioService.FileObjectInfo("${hiCMapCreationForm.name}.hic", file.size, file.inputStream))
            .run { ResponseEntity.ok(this.toInfoDto()) }
    }
}
