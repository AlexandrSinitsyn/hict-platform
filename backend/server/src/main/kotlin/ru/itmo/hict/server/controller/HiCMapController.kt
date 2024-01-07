package ru.itmo.hict.server.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.hict.dto.HiCMapInfoDto
import ru.itmo.hict.dto.HiCMapInfoDto.Companion.toInfoDto
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.HiCMapCreationForm
import ru.itmo.hict.server.service.HiCMapService

@RestController
@RequestMapping("/api/v1/hi-c")
class HiCMapController(
    private val hiCMapService: HiCMapService,
) {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<HiCMapInfoDto>> =
        hiCMapService.getAll().map { it.toInfoDto() }.run { ResponseEntity.ok(this) }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long): ResponseEntity<HiCMapInfoDto> =
        hiCMapService.getById(id)?.run { ResponseEntity.ok(this.toInfoDto()) }
            ?: ResponseEntity.notFound().build()

    @PostMapping("/publish")
    fun publish(@RequestBody hiCMapCreationForm: HiCMapCreationForm,
                bindingResult: BindingResult): ResponseEntity<HiCMapInfoDto> {
        val user = requestUserInfo.user

        if (user == null) {
            bindingResult.reject("not-authorized",
                "You must be authorized for publishing a Hi-C map")
        }

        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }

        return hiCMapService.save(user!!, hiCMapCreationForm.name, hiCMapCreationForm.description)
            .run { ResponseEntity.ok(this.toInfoDto()) }
    }
}
