package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.GroupInfoDto
import ru.itmo.hict.dto.GroupInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.UserInfoDto
import ru.itmo.hict.dto.UserInfoDto.Companion.toInfoDto
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.*
import ru.itmo.hict.server.service.GroupService

@RestController
@RequestMapping("/api/v1/groups")
class GroupController(
    private val groupService: GroupService,
) : ApiExceptionController() {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    private fun <T> authorized(method: User.() -> T): T = requestUserInfo.user?.run(method)
        ?: throw ValidationException(DirectFieldBindingResult(this, "group-controller").apply {
            reject("not-authorized", "You should be authorized to do this action")
        })

    @GetMapping("/all")
    fun all(): ResponseEntity<List<GroupInfoDto>> = authorized {
        groupService.getAll().map { it.toInfoDto() }
    }.run { ResponseEntity.ok(this) }

    @PostMapping("/new")
    fun new(@RequestBody @Valid groupCreationForm: GroupCreationForm): ResponseEntity<GroupInfoDto> = authorized {
        groupService.create(this, groupCreationForm.name)
    }.run { ResponseEntity.ok(this.toInfoDto()) }

    @PatchMapping("/{name}/update/name")
    fun updateName(@PathVariable("name") name: String,
                   @RequestBody @Valid groupUpdateNameForm: GroupUpdateNameForm): ResponseEntity<Boolean> = authorized {
        groupService.updateName(name, groupUpdateNameForm.name)
    }.run { ResponseEntity.ok(true) }

    @PostMapping("/{name}/join")
    fun join(@PathVariable("name") name: String): ResponseEntity<Boolean> = authorized {
        groupService.join(this, name)
    }.run { ResponseEntity.ok(true) }
}
