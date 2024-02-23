package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.UpdateEmailForm
import ru.itmo.hict.server.form.UpdateLoginForm
import ru.itmo.hict.server.form.UpdatePasswordForm
import ru.itmo.hict.server.form.UpdateUsernameForm
import ru.itmo.hict.server.service.UserService

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    @GetMapping("/count")
    fun count(): ResponseEntity<Long> = userService.count().run { ResponseEntity.ok(this) }

    private fun authorized(bindingResult: BindingResult): User {
        requestUserInfo.user?.let { return it }

        bindingResult.reject("not-authorized", "You must be authorized to update your profile")
        throw ValidationException(bindingResult)
    }

    private fun notSame(field: String, bindingResult: BindingResult, test: () -> Boolean) {
        if (!test()) {
            bindingResult.reject("same-$field", "New $field should be different")
        }
    }

    private fun checkNoErrors(bindingResult: BindingResult) {
        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }
    }

    @PatchMapping("/update/username")
    fun updateUsername(@RequestBody @Valid form: UpdateUsernameForm,
                       bindingResult: BindingResult): ResponseEntity<Boolean> {
        val user = authorized(bindingResult)

        notSame("username", bindingResult) { user.username != form.username }

        checkNoErrors(bindingResult)

        return userService.updateUsername(user, form.username).run { ResponseEntity.ok(this) }
    }

    @PatchMapping("/update/login")
    fun updateLogin(@RequestBody @Valid form: UpdateLoginForm,
                    bindingResult: BindingResult): ResponseEntity<Boolean> {
        val user = authorized(bindingResult)

        notSame("login", bindingResult) { user.login != form.login }

        checkNoErrors(bindingResult)

        return userService.updateLogin(user, form.login).run { ResponseEntity.ok(this) }
    }

    @PatchMapping("/update/email")
    fun updateEmail(@RequestBody @Valid form: UpdateEmailForm,
                    bindingResult: BindingResult): ResponseEntity<Boolean> {
        val user = authorized(bindingResult)

        notSame("email", bindingResult) { user.email != form.email }

        checkNoErrors(bindingResult)

        return userService.updateEmail(user, form.email).run { ResponseEntity.ok(this) }
    }

    @PatchMapping("/update/password")
    fun updatePassword(@RequestBody @Valid form: UpdatePasswordForm,
                       bindingResult: BindingResult): ResponseEntity<Boolean> {
        val user = authorized(bindingResult)

        if (!userService.checkCredentials(user, form.oldPassword)) {
            bindingResult.reject("invalid-password",
                "You must confirm this action with a password")
        }

        notSame("password", bindingResult) { user.password != form.newPassword }

        checkNoErrors(bindingResult)

        return userService.updatePassword(user, form.oldPassword, form.newPassword)
            .run { ResponseEntity.ok(this) }
    }
}
