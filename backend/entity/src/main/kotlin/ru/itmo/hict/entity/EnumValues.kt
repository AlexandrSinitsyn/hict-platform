package ru.itmo.hict.entity

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumValuesValidator::class])
annotation class EnumValues(
    val clazz: KClass<out Enum<*>>,
    val message: String = "must be null or not blank",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class EnumValuesValidator : ConstraintValidator<EnumValues, String> {
    private lateinit var values: List<String>

    override fun initialize(constraintAnnotation: EnumValues) {
        values = constraintAnnotation.clazz.java.enumConstants?.map { it.name } ?: listOf()
    }

    override fun isValid(s: String?, context: ConstraintValidatorContext): Boolean =
        s?.run { values.any { s in it } } ?: false
}
