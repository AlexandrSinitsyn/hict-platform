package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.ContactMapInfoDto
import ru.itmo.hict.dto.ContactMapInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.ExperimentInfoDto
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.service.ContactMapService
import ru.itmo.hict.server.service.GrpcContainerService
import ru.itmo.hict.server.validator.ContactMapCreationFormValidator

@RestController
@RequestMapping("/api/v1/contact-map")
class ContactMapController(
    private val contactMapService: ContactMapService,
    private val contactMapCreationFormValidator: ContactMapCreationFormValidator,
    private val containerService: GrpcContainerService,
) : ApiExceptionController() {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    @InitBinder("hiCMapCreationForm")
    fun initPublishBinder(webDataBinder: WebDataBinder) {
        webDataBinder.addValidators(contactMapCreationFormValidator)
    }

    @GetMapping("/acquire/{name}")
    fun getByName(@PathVariable("name") name: String): ResponseEntity<ContactMapInfoDto> {
        val user = requestUserInfo.user
            ?: throw ValidationException(DirectFieldBindingResult(this, "dind").apply {
                reject("not-authorized", "You should be authorized to do this action")
            })

        val hiCMap = contactMapService.getByName(name)
            ?: return ResponseEntity.notFound().build()

        contactMapService.view(hiCMap)

        containerService.publish(user)

        return ResponseEntity.ok(hiCMap.toInfoDto())
    }

    @GetMapping("/acquire/{name}/ping")
    fun ping(@PathVariable("name") name: String) {
        val user = requestUserInfo.user
            ?: throw ValidationException(DirectFieldBindingResult(this, "dind").apply {
                reject("not-authorized", "You should be authorized to do this action")
            })

        containerService.ping(user)
    }

    @PostMapping("/new")
    fun publish(@RequestBody @Valid experimentInfoDto: ExperimentInfoDto): ResponseEntity<ContactMapInfoDto> =
        contactMapService.create(experimentInfoDto.name)?.run { ResponseEntity.ok(this.toInfoDto()) }
            ?: throw ValidationException(DirectFieldBindingResult(this, "experiment").apply {
                reject("unknown-experiment", "No experiment found for: ${experimentInfoDto.name}")
            })
}
