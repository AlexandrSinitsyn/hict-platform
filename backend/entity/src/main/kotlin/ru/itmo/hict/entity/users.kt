package ru.itmo.hict.entity

import jakarta.annotation.Nullable
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp
import java.util.UUID

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id"]),
        UniqueConstraint(columnNames = ["login"]),
        UniqueConstraint(columnNames = ["email"]),
        UniqueConstraint(columnNames = ["visualization_settings_id"]),
    ],
    indexes = [
        Index(name = "user_by_id", columnList = "user_id", unique = true),
        Index(name = "user_by_login", columnList = "login,user_id", unique = true),
        Index(name = "user_by_email", columnList = "email,user_id", unique = true),
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
    @Size(min = 3, max = 64)
    @Column(name = "password", nullable = false)
    val password: String,

    @NotNull
    @ManyToMany(
        fetch = FetchType.EAGER,
        cascade = [CascadeType.DETACH],
        mappedBy = "users"
    )
    val groups: List<Group> = listOf(),

    @Nullable
    @Column(name = "visualization_settings_id", nullable = true)
    val visualizationSettings: UUID? = null,

    @NotNull
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    @Column(name = "user_id", nullable = false)
    val id: UUID? = null,

    @NotNull
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false)
    val creationTime: Timestamp? = null,
)

@Entity
@Table(
    name = "groups",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["group_id"]),
        UniqueConstraint(columnNames = ["group_name"]),
    ],
    indexes = [
        Index(name = "group_by_id", columnList = "group_id", unique = true),
        Index(name = "group_by_name", columnList = "group_name,group_id", unique = true),
    ],
)
class Group(
    @NotNull
    @NotBlank
    @Size(min = 3, max = 256)
    @Column(name = "group_name", nullable = false)
    val name: String,

    @NotNull
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.DETACH],
    )
    @JoinTable(
        name = "user_groups",
        joinColumns = [
            JoinColumn(name = "group_id", nullable = false)
        ],
        inverseJoinColumns = [
            JoinColumn(name = "user_id", nullable = false)
        ],
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["group_id"]),
            UniqueConstraint(columnNames = ["user_id"]),
        ],
        indexes = [
            Index(name = "user_by_group", columnList = "group_id,user_id", unique = true),
            Index(name = "group_by_user", columnList = "user_id,group_id", unique = true),
        ],
    )
    val users: List<User> = listOf(),

    @NotNull
    @NotBlank
    @Size(min = 3, max = 256)
    @Column(name = "affiliation", nullable = true)
    val affiliation: String? = null,

    @NotNull
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    @Column(name = "group_id", nullable = false)
    val id: UUID? = null,

    @NotNull
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false)
    val creationTime: Timestamp? = null,
)
