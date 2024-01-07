package ru.itmo.hict.authorization.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.hict.authorization.exceptions.ValidationException
import ru.itmo.hict.authorization.form.EnterForm
import ru.itmo.hict.authorization.form.RegisterForm
import ru.itmo.hict.authorization.service.UserService
import ru.itmo.hict.authorization.validators.EnterFormValidator
import ru.itmo.hict.authorization.validators.RegisterFormValidator
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.dto.JwtService
import ru.itmo.hict.dto.UserInfoDto.Companion.toInfoDto

@RestController
@RequestMapping("/api/v1/auth")
class UserController(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val registerFormValidator: RegisterFormValidator,
    private val enterFormValidator: EnterFormValidator,
) {
    @InitBinder("registerForm")
    fun initRegisterBinder(webDataBinder: WebDataBinder) {
        webDataBinder.addValidators(registerFormValidator)
    }

    @InitBinder("enterForm")
    fun initBinder(webDataBinder: WebDataBinder) {
        webDataBinder.addValidators(enterFormValidator)
    }

    @PostMapping("/register")
    fun register(@RequestBody @Valid registerForm: RegisterForm, bindingResult: BindingResult): ResponseEntity<Jwt> {
        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }

        return userService.register(registerForm.username, registerForm.login, registerForm.email, registerForm.passwordSha)
            .run { ResponseEntity.ok(jwtService.create(this.toInfoDto())) }
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid enterForm: EnterForm, bindingResult: BindingResult): ResponseEntity<Jwt> {
        if (bindingResult.hasErrors()) {
            throw ValidationException(bindingResult)
        }

        return userService.findByCredentials(enterForm.login, enterForm.email, enterForm.passwordSha).get()
            .run { ResponseEntity.ok(jwtService.create(this.toInfoDto())) }
    }
}
