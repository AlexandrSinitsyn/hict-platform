package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.UserInfoDto
import ru.itmo.hict.dto.UserInfoDto.Companion.toInfoDto
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.*
import ru.itmo.hict.server.service.UserService
import ru.itmo.hict.server.validator.UpdateUserInfoFormValidator

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val updateUserInfoFormValidator: UpdateUserInfoFormValidator,
) : ApiExceptionController() {
    @InitBinder("updateUserInfoForm")
    fun initPublishBinder(webDataBinder: WebDataBinder) {
        webDataBinder.addValidators(updateUserInfoFormValidator)
    }

    private fun notSame(field: String, bindingResult: BindingResult): Unit =
        bindingResult.reject("same-$field", "New $field should be different")

    @GetMapping("/count")
    fun count(): ResponseEntity<Long> = userService.count().run { ResponseEntity.ok(this) }

    @GetMapping("/self")
    fun self(): ResponseEntity<UserInfoDto> = authorized { this }.toInfoDto().response()

    @GetMapping("/all")
    fun all(): ResponseEntity<List<UserInfoDto>> = authorized { userService.getAll().map { it.toInfoDto() } }.response()

    @PatchMapping("/update/info")
    fun updateInfo(@RequestBody @Valid updateUserInfoForm: UpdateUserInfoForm,
                   bindingResult: BindingResult): ResponseEntity<Boolean> = authorized {
        when {
            this.username != updateUserInfoForm.username -> notSame("username", bindingResult)
            this.login != updateUserInfoForm.login -> notSame("login", bindingResult)
            this.email != updateUserInfoForm.email -> notSame("email", bindingResult)
        }

        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }

        updateUserInfoForm.let {
            userService.updateUsername(this, it.username!!)
            userService.updateLogin(this, it.login!!)
            userService.updateEmail(this, it.email!!)
        }
    }.success()

    @PatchMapping("/update/password")
    fun updatePassword(@RequestBody @Valid form: UpdatePasswordForm,
                       bindingResult: BindingResult): ResponseEntity<Boolean> = authorized {
        if (!userService.checkCredentials(this, form.oldPassword)) {
            bindingResult.reject("invalid-password",
                "You must confirm this action with a password")
        }

        if (form.oldPassword != form.newPassword) {
            notSame("password", bindingResult)
        }

        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }

        userService.updatePassword(this, form.oldPassword, form.newPassword)
    }.response()
}
