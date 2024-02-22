package ru.itmo.hict.authorization.validators

import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import ru.itmo.hict.authorization.form.EnterForm
import ru.itmo.hict.authorization.logging.Info
import ru.itmo.hict.authorization.service.UserService

@Component
class EnterFormValidator(
    private val userService: UserService,
) : Validator {
    override fun supports(clazz: Class<*>): Boolean = EnterForm::class.java == clazz

    @Info("enter", "form validation")
    override fun validate(target: Any, errors: Errors) {
        if (!errors.hasErrors()) {
            val form = target as EnterForm

            if (form.login == null && form.email == null) {
                errors.reject("invalid-enter-form", "EnterForm require at least one of [login, email]")
            }

            if (userService.findByCredentials(form.login, form.email, form.password).isEmpty) {
                errors.reject("invalid-login-or-password", "Invalid login or password")
            }
        }
    }
}
