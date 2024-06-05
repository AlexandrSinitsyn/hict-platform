package ru.itmo.hict.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.itmo.hict.dto.FileType
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [StringEnumValuesValidator::class, FileTypeEnumValuesValidator::class])
annotation class EnumValues(
    val clazz: KClass<out Enum<*>>,
    val message: String = "must be null or not blank",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

abstract class AbstractEnumValuesValidator<T> : ConstraintValidator<EnumValues, T> {
    protected lateinit var values: List<String>

    override fun initialize(constraintAnnotation: EnumValues) {
        values = constraintAnnotation.clazz.java.enumConstants?.map { it.name } ?: listOf()
    }
}

class StringEnumValuesValidator : AbstractEnumValuesValidator<String>() {
    override fun isValid(s: String?, context: ConstraintValidatorContext): Boolean =
        s?.run { values.any { s in it } } ?: false
}

class FileTypeEnumValuesValidator : AbstractEnumValuesValidator<FileType>() {
    override fun isValid(type: FileType?, context: ConstraintValidatorContext): Boolean =
        type?.run { values.any { this.name in it } } ?: false
}
