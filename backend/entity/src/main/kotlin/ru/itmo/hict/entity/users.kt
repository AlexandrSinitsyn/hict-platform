package ru.itmo.hict.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

enum class Role {
    ANONYMOUS,
    USER,
    ADMIN,
    SUPERUSER
}

@Entity
@Table(name = "users")
class User(
    @NotNull @NotBlank                                                  val username: String,
    @NotNull @NotBlank                                                  val login: String,
    @NotNull @NotBlank                                                  val email: String,
    @NotNull @NotBlank                                                  val passwordSha: String,
    @NotNull                                                            val role: Role,
    @NotNull @GeneratedValue(strategy = GenerationType.IDENTITY)    @Id val id: Long? = null,
    @NotNull                                         @CreationTimestamp val creationTime: Timestamp? = null,
)

