package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.validation.BindingResult

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.ExperimentInfoDto
import ru.itmo.hict.dto.ExperimentInfoDto.Companion.toInfoDto
import ru.itmo.hict.server.form.*
import ru.itmo.hict.server.service.ExperimentService

@RestController
@RequestMapping("/api/v1/experiment")
class ExperimentController(
    private val experimentService: ExperimentService,
) : ApiExceptionController() {
    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<ExperimentInfoDto>> = experimentService.getAll().map { it.toInfoDto() }.response()

    @PostMapping("/new")
    fun create(): ResponseEntity<ExperimentInfoDto> = authorized { experimentService.create(this) }.toInfoDto().response()

    @PatchMapping("/{id}/update/name")
    fun updateName(
        @PathVariable("id") id: Long,
        @RequestBody @Valid experimentNameUpdateForm: ExperimentNameUpdateForm,
        bindingResult: BindingResult,
    ): ResponseEntity<Boolean> = authorized { experimentService.updateName(id, experimentNameUpdateForm.name) }.success()

    @PatchMapping("/{id}/update/info")
    fun updateInfo(
        @PathVariable("id") id: Long,
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
