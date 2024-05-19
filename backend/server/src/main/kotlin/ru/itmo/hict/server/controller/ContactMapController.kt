package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.ContactMapInfoDto
import ru.itmo.hict.dto.ContactMapInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.ExperimentInfoDto
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.ContactMapInfoUpdateForm
import ru.itmo.hict.server.form.ContactMapNameUpdateForm
import ru.itmo.hict.server.form.ExperimentInfoUpdateForm
import ru.itmo.hict.server.form.ExperimentNameUpdateForm
import ru.itmo.hict.server.service.ContactMapService
import ru.itmo.hict.server.service.GrpcContainerService

@RestController
@RequestMapping("/api/v1/contact-map")
class ContactMapController(
    private val contactMapService: ContactMapService,
    private val containerService: GrpcContainerService,
) : ApiExceptionController() {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    private fun <T> authorized(method: User.() -> T): T = requestUserInfo.user?.run(method)
        ?: throw ValidationException(DirectFieldBindingResult(this, "experiment-controller").apply {
            reject("not-authorized", "You should be authorized to do this action")
        })

    @GetMapping("/acquire/{name}")
    fun getByName(@PathVariable("name") name: String): ResponseEntity<ContactMapInfoDto> = authorized {
        val hiCMap = contactMapService.getByName(name)
            ?: return@authorized ResponseEntity.notFound().build()

        contactMapService.view(hiCMap)

        containerService.publish(this)

        return@authorized ResponseEntity.ok(hiCMap.toInfoDto())
    }

    @GetMapping("/acquire/{name}/ping")
    fun ping(@PathVariable("name") name: String) = authorized { containerService.ping(this) }

    @PostMapping("/new")
    fun publish(@RequestBody @Valid experimentInfoDto: ExperimentInfoDto): ResponseEntity<ContactMapInfoDto> =
        contactMapService.create(experimentInfoDto.name)?.run { ResponseEntity.ok(this.toInfoDto()) }
            ?: throw ValidationException(DirectFieldBindingResult(this, "experiment").apply {
                reject("unknown-experiment", "No experiment found for: ${experimentInfoDto.name}")
            })

    @PatchMapping("/{id}/update/name")
    fun updateName(@PathVariable("id") id: Long,
                   @RequestBody @Valid contactMapNameUpdateForm: ContactMapNameUpdateForm,
                   bindingResult: BindingResult
    ): ResponseEntity<Boolean> =
        authorized { contactMapService.updateName(id, contactMapNameUpdateForm.name) }
            .run { ResponseEntity.ok(true) }

    @PatchMapping("/{id}/update/info")
    fun updateInfo(@PathVariable("id") id: Long,
                   @RequestBody @Valid contactMapInfoUpdateForm: ContactMapInfoUpdateForm,
                   bindingResult: BindingResult
    ): ResponseEntity<Boolean> =
        authorized { contactMapService.updateInfo(id, contactMapInfoUpdateForm.description, contactMapInfoUpdateForm.link) }
            .run { ResponseEntity.ok(true) }
}
