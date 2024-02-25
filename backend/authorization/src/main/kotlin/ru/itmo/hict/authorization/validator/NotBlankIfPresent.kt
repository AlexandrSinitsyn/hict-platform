package ru.itmo.hict.authorization.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotBlankIfPresentValidator::class])
annotation class NotBlankIfPresent(
    val message: String = "must be null or not blank",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class NotBlankIfPresentValidator : ConstraintValidator<NotBlankIfPresent, String> {
    override fun isValid(s: String?, context: ConstraintValidatorContext): Boolean = s?.isNotBlank() ?: true
}
