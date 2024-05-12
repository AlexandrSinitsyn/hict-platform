package ru.itmo.hict.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

@Entity
@Table(
    name = "species",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["species_id"]),
        UniqueConstraint(columnNames = ["tax_id"]),
        UniqueConstraint(columnNames = ["species_name"]),
    ],
    indexes = [
        Index(name = "species_by_id", columnList = "species_id", unique = true),
        Index(name = "species_by_tax_id", columnList = "tax_id", unique = true),
        Index(name = "species_by_name", columnList = "species_name,species_id", unique = true),
    ],
)
class Species(
    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "tax_id", unique = true, nullable = false)
    val taxId: String,

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "species_name", unique = true, nullable = false)
    val speciesName: String,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "species_id", unique = true, nullable = false)
    val id: Long? = null,
)

@Entity
@Table(
    name = "biosamples",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["biosample_id"]),
        UniqueConstraint(columnNames = ["species_id"]),
    ],
    indexes = [
        Index(name = "biosamples_by_id", columnList = "biosample_id", unique = true),
    ],
)
class Biosample(
    @NotNull
    @Column(name = "species_id", unique = true, nullable = false)
    val speciesId: Long,

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
    @Column(name = "species_id", unique = true, nullable = false)
    val id: Long? = null,

    @NotNull
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false)
    val creationTime: Timestamp? = null,
)
