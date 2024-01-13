package ru.itmo.hict.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

enum class Role {
    ANONYMOUS,
    USER,
    ADMIN,
    SUPERUSER
}

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["id"]),
        UniqueConstraint(columnNames = ["login"]),
        UniqueConstraint(columnNames = ["email"]),
    ],
    indexes = [
        Index(name = "users_by_id", columnList = "user_id", unique = true),
        Index(name = "users_by_login", columnList = "login,user_id", unique = true),
        Index(name = "users_by_email", columnList = "email,user_id", unique = true),
    ],
)
class User(
    @NotNull
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "username", nullable = false)
    val username: String,

    @NotNull
    @NotBlank
    @Size(min = 3, max = 64)
    @Column(name = "login", unique = true, nullable = false)
    val login: String,

    @NotNull
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "email", unique = true, nullable = false)
    val email: String,

    @NotNull
    @NotBlank
    @Column(name = "password_sha", nullable = false)
    val passwordSha: String,

    @NotNull
    @Column(name = "role", columnDefinition = "TINYINT", nullable = false)
    val role: Role,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "user_id", nullable = false)
    val id: Long? = null,

    @NotNull
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false)
    val creationTime: Timestamp? = null,
)
