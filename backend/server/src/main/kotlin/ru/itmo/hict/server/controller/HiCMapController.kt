package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.itmo.hict.dto.HiCMapInfoDto
import ru.itmo.hict.dto.HiCMapInfoDto.Companion.toInfoDto
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.HiCMapCreationForm
import ru.itmo.hict.server.service.HiCMapService
import ru.itmo.hict.server.service.KafkaPublisher
import ru.itmo.hict.server.service.MinioService
import ru.itmo.hict.server.validator.HiCMapCreationFormValidator

@RestController
@RequestMapping("/api/v1/hi-c")
class HiCMapController(
    private val hiCMapService: HiCMapService,
    private val hiCMapCreationFormValidator: HiCMapCreationFormValidator,
    private val kafkaPublisher: KafkaPublisher,
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

    @GetMapping("/acquire/{name}")
    fun getByName(@PathVariable("name") name: String): ResponseEntity<HiCMapInfoDto> {
        val user = requestUserInfo.user
            ?: throw ValidationException(DirectFieldBindingResult(this, "dind").apply {
                reject("not-authorized", "You should be authorized to do this action")
            })

        val hiCMap = hiCMapService.getByName(name)
            ?: return ResponseEntity.notFound().build()

        hiCMapService.view(hiCMap)

        kafkaPublisher.publish(user)

        return ResponseEntity.ok(hiCMap.toInfoDto())
    }

    @GetMapping("/acquire/{name}/ping")
    fun ping(@PathVariable("name") name: String) {
        val user = requestUserInfo.user
            ?: throw ValidationException(DirectFieldBindingResult(this, "dind").apply {
                reject("not-authorized", "You should be authorized to do this action")
            })

        kafkaPublisher.ping(user)
    }

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
