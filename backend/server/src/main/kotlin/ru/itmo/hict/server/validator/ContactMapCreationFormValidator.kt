package ru.itmo.hict.server.validator

import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import ru.itmo.hict.server.form.HiCMapCreationForm
import ru.itmo.hict.server.logging.Info
import ru.itmo.hict.server.service.HiCMapService

@Component
class ContactMapCreationFormValidator(
    private val hiCMapService: HiCMapService,
) : Validator {
    override fun supports(clazz: Class<*>): Boolean = HiCMapCreationForm::class.java == clazz

    @Info("uploading", "form validation")
    override fun validate(target: Any, errors: Errors) {
        if (!errors.hasErrors()) {
            val form = target as HiCMapCreationForm

            if (!hiCMapService.checkUnique(form.name)) {
                errors.reject("unique-hi-c-map-name", "Hi-C map name must be unique")
            }
        }
    }
}
