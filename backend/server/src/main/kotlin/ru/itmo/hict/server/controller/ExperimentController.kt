package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.validation.BindingResult

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.ExperimentInfoDto
import ru.itmo.hict.dto.ExperimentInfoDto.Companion.toInfoDto
import ru.itmo.hict.server.form.*
import ru.itmo.hict.server.service.ExperimentService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/experiment")
@CrossOrigin
class ExperimentController(
    private val experimentService: ExperimentService,
) : ApiExceptionController() {
    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<ExperimentInfoDto>> =
        experimentService.getAll(requestUserInfo.user?.groups).map { it.toInfoDto() }.response()

    @PostMapping("/new")
    fun create(@RequestBody @Valid experimentForm: ExperimentCreationForm): ResponseEntity<ExperimentInfoDto> =
        authorized { experimentService.create(this, experimentForm.groupName) }.toInfoDto().response()

    @PatchMapping("/{id}/update/name")
    fun updateName(
        @PathVariable("id") id: UUID,
        @RequestBody @Valid experimentNameUpdateForm: ExperimentNameUpdateForm,
        bindingResult: BindingResult,
    ): ResponseEntity<Boolean> = authorized { experimentService.updateName(id, experimentNameUpdateForm.name) }.success()

    @PatchMapping("/{id}/update/info")
    fun updateInfo(
        @PathVariable("id") id: UUID,
        @RequestBody @Valid experimentInfoUpdateForm: ExperimentInfoUpdateForm,
        bindingResult: BindingResult,
    ): ResponseEntity<Boolean> = authorized {
            experimentService.updateInfo(id, experimentInfoUpdateForm.description,
                experimentInfoUpdateForm.paper, experimentInfoUpdateForm.acknowledgement)
        }.success()

    // @PatchMapping("/{id}/update/contact-person")
    // fun updateContactPerson(@PathVariable("id") id: Long,
    //                         @RequestBody @Valid contactPersonForm: ContactPersonForm,
    //                         bindingResult: BindingResult
    // ): ResponseEntity<Boolean> =
    //     authorized { experimentService.setContactPerson(id, contactPersonForm.name, contactPersonForm.email) }
    //         .run { ResponseEntity.ok(true) }
}
