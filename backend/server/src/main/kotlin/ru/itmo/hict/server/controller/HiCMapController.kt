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
import ru.itmo.hict.server.service.FileService
import ru.itmo.hict.server.service.HiCMapService
import ru.itmo.hict.server.validator.HiCMapCreationFormValidator
import java.io.BufferedOutputStream
import java.io.FileOutputStream

@RestController
@RequestMapping("/api/v1/hi-c")
class HiCMapController(
    private val hiCMapService: HiCMapService,
    private val hiCMapCreationFormValidator: HiCMapCreationFormValidator,
    private val fileService: FileService,
) {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    @InitBinder("hiCMapCreationForm")
    fun initRegisterBinder(webDataBinder: WebDataBinder) {
        webDataBinder.addValidators(hiCMapCreationFormValidator)
    }

    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<HiCMapInfoDto>> =
        hiCMapService.getAll().map { it.toInfoDto() }.run { ResponseEntity.ok(this) }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long): ResponseEntity<HiCMapInfoDto> =
        hiCMapService.getById(id)?.run { ResponseEntity.ok(this.toInfoDto()) }
            ?: ResponseEntity.notFound().build()

    @PostMapping("/publish")
    fun publish(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("form") @Valid hiCMapCreationForm: HiCMapCreationForm,
        bindingResult: BindingResult,
    ): ResponseEntity<HiCMapInfoDto> {
        val user = requestUserInfo.user

        when {
            user == null -> bindingResult.reject("not-authorized",
                "You must be authorized for publishing a Hi-C map")
            file.isEmpty -> bindingResult.reject("empty-file", "File should not be empty")
        }

        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }

        val savedFile = fileService.tmp(hiCMapCreationForm.name).toFile()

        BufferedOutputStream(FileOutputStream(savedFile)).use {
            it.write(file.bytes)
        }

        return hiCMapService.load(user!!, hiCMapCreationForm.name, hiCMapCreationForm.description, savedFile)
            .run { ResponseEntity.ok(this.toInfoDto()) }
    }
}
