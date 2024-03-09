package ru.itmo.hict.server.validator

import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import ru.itmo.hict.server.form.UpdateUserInfoForm
import ru.itmo.hict.server.logging.Info
import ru.itmo.hict.server.service.UserService

@Component
class UpdateUserInfoFormValidator(
    private val userService: UserService,
) : Validator {
    override fun supports(clazz: Class<*>): Boolean = UpdateUserInfoForm::class.java == clazz

    @Info("updateUserInfo", "form validation")
    override fun validate(target: Any, errors: Errors) {
        if (!errors.hasErrors()) {
            val form = target as UpdateUserInfoForm

            if (form.username == null && form.login == null && form.email == null) {
                errors.reject("invalid-update-user-info-form",
                    "UpdateUserInfoForm require at least one of [username, login, email]")
            }

            if (!userService.isUniqueLoginAndEmail(form.login, form.email)) {
                errors.reject("occupied-login-or-email", "Already taken login or email")
            }
        }
    }
}
