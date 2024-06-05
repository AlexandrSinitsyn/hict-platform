package ru.itmo.hict.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import java.util.UUID

@Entity
@Table(
    name = "contact_map_views",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["contact_map_id"])
    ],
    indexes = [
        Index(name = "contact_map_views_by_id", columnList = "contact_map_id", unique = true),
    ],
)
class ContactMapViews(
    @NotNull
    @Id
    @Column(name = "contact_map_id", insertable = false, updatable = false)
    private val contactMapId: UUID,

    @NotNull
    @OneToOne(
        optional = false,
        cascade = [CascadeType.DETACH],
        orphanRemoval = false,
    )
    @JoinColumn(name = "contact_map_id", unique = true, nullable = false)
    val contactMap: ContactMap,

    @NotNull
    @PositiveOrZero
    @Column(name = "count", nullable = false)
    val count: Long,

    @NotNull
    @UpdateTimestamp
    @Column(name = "last_seen", nullable = false)
    val lastSeen: Timestamp? = null,
)


@Entity
@Table(
    name = "tags",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["tag_id"]),
        UniqueConstraint(columnNames = ["tag_name"]),
    ],
    indexes = [
        Index(name = "tags_by_id", columnList = "tag_id", unique = true),
        Index(name = "tags_by_name", columnList = "tag_name,tag_id", unique = true),
    ],
)
class ContactMapTag(
    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "tag_name", unique = true, nullable = false)
    val name: String,

    @NotNull
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    @Column(name = "tag_id", unique = true, nullable = false)
    val id: UUID? = null,

    @NotNull
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false)
    val creationTime: Timestamp? = null,
)
