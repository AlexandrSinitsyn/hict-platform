package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.*
import ru.itmo.hict.server.form.UserExtendedInfo.Companion.toExtendedInfo
import ru.itmo.hict.server.service.UserService
import ru.itmo.hict.server.validator.UpdateUserInfoFormValidator

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val updateUserInfoFormValidator: UpdateUserInfoFormValidator,
) : ApiExceptionController() {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    @InitBinder("form")
    fun initPublishBinder(webDataBinder: WebDataBinder) {
        webDataBinder.addValidators(updateUserInfoFormValidator)
    }

    @GetMapping("/count")
    fun count(): ResponseEntity<Long> = userService.count().run { ResponseEntity.ok(this) }

    @GetMapping("/self")
    fun self(): ResponseEntity<UserExtendedInfo> =
        authorized(DirectFieldBindingResult(this, "jwt")).run { ResponseEntity.ok(this.toExtendedInfo()) }

    private fun authorized(bindingResult: BindingResult): User {
        requestUserInfo.user?.let { return it }

        bindingResult.reject("not-authorized", "You must be authorized to update your profile")
        throw ValidationException(bindingResult)
    }

    private fun notSame(field: String, bindingResult: BindingResult, test: () -> Boolean) {
        if (!test()) {
            bindingResult.reject("same-$field", "New $field should be different")
            throw ValidationException(bindingResult)
        }
    }

    private fun checkNoErrors(bindingResult: BindingResult) {
        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }
    }

    @PatchMapping("/update/info")
    fun updateInfo(@RequestBody @Valid form: UpdateUserInfoForm,
                   bindingResult: BindingResult): ResponseEntity<Boolean> {
        checkNoErrors(bindingResult)

        val user = authorized(bindingResult)

        form.username?.let {
            notSame("username", bindingResult) { user.username != it }

            userService.updateUsername(user, it)
        }

        form.login?.let {
            notSame("login", bindingResult) { user.login != it }

            userService.updateLogin(user, it)
        }

        form.email?.let {
            notSame("email", bindingResult) { user.email != it }

            userService.updateEmail(user, it)
        }

        return ResponseEntity.ok(true)
    }

    @PatchMapping("/update/password")
    fun updatePassword(@RequestBody @Valid form: UpdatePasswordForm,
                       bindingResult: BindingResult): ResponseEntity<Boolean> {
        val user = authorized(bindingResult)

        if (!userService.checkCredentials(user, form.oldPassword)) {
            bindingResult.reject("invalid-password",
                "You must confirm this action with a password")
        }

        notSame("password", bindingResult) { form.oldPassword != form.newPassword }

        checkNoErrors(bindingResult)

        return userService.updatePassword(user, form.oldPassword, form.newPassword)
            .run { ResponseEntity.ok(this) }
    }
}
