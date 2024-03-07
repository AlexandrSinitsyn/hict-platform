package ru.itmo.hict.entity

import jakarta.annotation.Nullable
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp

@Entity
@Table(
    name = "hi_c_maps",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["id"]),
        UniqueConstraint(columnNames = ["name"]),
    ],
    indexes = [
        Index(name = "hi_c_map_by_id", columnList = "hi_c_map_id", unique = true),
        Index(name = "hi_c_map_by_name", columnList = "name", unique = true),
    ],
)
class HiCMap(
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "user_id",
        nullable = false,
    )
    val author: User,

    @NotNull
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "name", unique = true, nullable = false)
    val name: String,

    @NotNull
    @NotBlank
    @Size(max = 65536)
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    val description: String,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "hi_c_map_id", unique = true, nullable = false)
    val id: Long? = null,

    @NotNull
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false)
    val creationTime: Timestamp? = null,

    @Transient
    @Nullable
    @OneToOne(
        mappedBy = "hiCMap",
        fetch = FetchType.LAZY,
        optional = true,
        cascade = [CascadeType.DETACH],
        orphanRemoval = false,
    )
    val views: Views? = null,
)

@Entity
@Table(
    name = "hi_c_map_views",
    indexes = [
        Index(name = "hi_c_map_views_by_id", columnList = "hic_map_id", unique = true),
    ],
)
class Views(
    @Id
    @Column(name = "hi_c_map_id", insertable = false, updatable = false)
    private val hiCMapId: Long,

    @NotNull
    @OneToOne(
        optional = false,
        cascade = [CascadeType.DETACH],
        orphanRemoval = false,
    )
    @JoinColumn(name = "hi_c_map_id", unique = true, nullable = false)
    val hiCMap: HiCMap,

    @NotNull
    @PositiveOrZero
    val count: Long,

    @NotNull
    @UpdateTimestamp
    @Column(name = "last_seen", nullable = false)
    val lastSeen: Timestamp? = null,
)
