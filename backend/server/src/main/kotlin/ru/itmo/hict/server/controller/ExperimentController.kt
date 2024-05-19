package ru.itmo.hict.server.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.ExperimentInfoDto
import ru.itmo.hict.dto.ExperimentInfoDto.Companion.toInfoDto
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.service.ExperimentService

@RestController
@RequestMapping("/api/v1/experiment")
class ExperimentController(
    private val experimentService: ExperimentService,
) : ApiExceptionController() {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    private fun <T> authorized(method: User.() -> T): T = requestUserInfo.user?.run(method)
        ?: throw ValidationException(DirectFieldBindingResult(this, "experiment-controller").apply {
            reject("not-authorized", "You should be authorized to do this action")
        })

    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<ExperimentInfoDto>> =
        experimentService.getAll().map { it.toInfoDto() }.run { ResponseEntity.ok(this) }

    @PostMapping("/new")
    fun create(): ResponseEntity<ExperimentInfoDto> =
        authorized { experimentService.create(this) }.run { ResponseEntity.ok(this.toInfoDto()) }
}
