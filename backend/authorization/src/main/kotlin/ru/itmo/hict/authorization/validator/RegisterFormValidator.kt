package ru.itmo.hict.authorization.validator

import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import ru.itmo.hict.authorization.form.RegisterForm
import ru.itmo.hict.authorization.logging.Info
import ru.itmo.hict.authorization.service.UserService

@Component
class RegisterFormValidator(
    private val userService: UserService,
) : Validator {
    override fun supports(clazz: Class<*>): Boolean = RegisterForm::class.java == clazz

    @Info("registration", "form validation")
    override fun validate(target: Any, errors: Errors) {
        if (!errors.hasErrors()) {
            val form = target as RegisterForm

            if (!userService.checkUnique(form.login, form.email)) {
                errors.reject("occupied-login-or-email", "Already occupied login or email")
            }
        }
    }
}
