package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.GroupInfoDto
import ru.itmo.hict.dto.GroupInfoDto.Companion.toInfoDto
import ru.itmo.hict.server.form.*
import ru.itmo.hict.server.service.GroupService

@RestController
@RequestMapping("/api/v1/groups")
class GroupController(
    private val groupService: GroupService,
) : ApiExceptionController() {
    @GetMapping("/all")
    fun all(): ResponseEntity<List<GroupInfoDto>> = authorized { groupService.getAll().map { it.toInfoDto() } }.response()

    @PostMapping("/new")
    fun new(@RequestBody @Valid groupCreationForm: GroupCreationForm): ResponseEntity<GroupInfoDto> =
        authorized { groupService.create(this, groupCreationForm.name) }.toInfoDto().response()

    @PatchMapping("/{name}/update/name")
    fun updateName(
        @PathVariable("name") name: String,
        @RequestBody @Valid groupUpdateNameForm: GroupUpdateNameForm,
    ): ResponseEntity<Boolean> = authorized { groupService.updateName(name, groupUpdateNameForm.name) }.success()

    @PostMapping("/{name}/join")
    fun join(@PathVariable("name") name: String): ResponseEntity<Boolean> =
        authorized { groupService.join(this, name) }.success()
}
