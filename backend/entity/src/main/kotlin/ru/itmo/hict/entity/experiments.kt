package ru.itmo.hict.entity

import jakarta.annotation.Nullable
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

@Entity
@Table(
    name = "experiments",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["experiment_id"]),
        UniqueConstraint(columnNames = ["experiment_name"]),
    ],
    indexes = [
        Index(name = "experiments_by_id", columnList = "experiment_id", unique = true),
        Index(name = "experiments_by_name", columnList = "experiment_name,experiment_id", unique = true),
    ],
)
class Experiment(
    @NotNull
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "experiment_name", unique = true, nullable = false)
    val name: String,

    @NotNull
    @NotBlank
    @Size(max = 65536)
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    val description: String,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "user_id",
        nullable = false,
    )
    val author: User,

    @NotNull
    @NotBlank
    @Size(max = 512)
    @Column(name = "paper", nullable = false)
    val paper: String,

    @NotNull
    @NotBlank
    @Size(max = 256)
    @Column(name = "attribution", nullable = false)
    val attribution: String,

    @NotNull
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.DETACH],
    )
    @JoinTable(
        name = "experiment_fasta",
        joinColumns = [
            JoinColumn(name = "experiment_id", nullable = false)
        ],
        inverseJoinColumns = [
            JoinColumn(name = "file_id", nullable = false)
        ],
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["experiment_id"]),
            UniqueConstraint(columnNames = ["file_id"]),
        ],
        indexes = [
            Index(name = "fasta_by_experiment", columnList = "experiment_id,file_id", unique = true),
        ],
    )
    val fasta: List<FastaFile> = listOf(),

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "experiment_id", unique = true, nullable = false)
    val id: Long? = null,

    @NotNull
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false)
    val creationTime: Timestamp? = null,

    @Transient
    @Nullable
    @OneToMany(
        mappedBy = "experiment",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.DETACH],
        orphanRemoval = false,
    )
    val contactMaps: List<ContactMap> = listOf(),
)

@Entity
@Table(
    name = "contact_maps",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["contact_map_id"]),
        UniqueConstraint(columnNames = ["contact_map_name"]),
    ],
    indexes = [
        Index(name = "contact_maps_by_id", columnList = "contact_map_id", unique = true),
        Index(name = "contact_maps_by_name", columnList = "contact_map_name,contact_map_id", unique = true),
        Index(name = "contact_maps_by_experiment", columnList = "experiment_id,contact_map_id", unique = true),
        Index(name = "contact_maps_by_species", columnList = "species_idcontact_map_id", unique = true),
    ],
)
class ContactMap(
    @NotNull
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "contact_map_name", unique = true, nullable = false)
    val name: String,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "experiment_id",
        nullable = false,
    )
    val experiment: Experiment,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "species_id",
        nullable = false,
    )
    val species: Species,

    @NotNull
    @NotBlank
    @Size(max = 65536)
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    val description: String,

    @NotNull
    @NotBlank
    @Size(max = 512)
    @Column(name = "hic_data_link", nullable = false)
    val hicDataLink: String,

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "biosample_id",
        nullable = true,
    )
    val biosample: Biosample? = null,

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "hict_id",
        nullable = true,
    )
    val hict: HiCTFile? = null,

    @NotNull
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.DETACH],
    )
    @JoinTable(
        name = "contact_map_agp",
        joinColumns = [
            JoinColumn(name = "contact_map_id", nullable = false)
        ],
        inverseJoinColumns = [
            JoinColumn(name = "file_id", nullable = false)
        ],
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["contact_map_id"]),
            UniqueConstraint(columnNames = ["file_id"]),
        ],
        indexes = [
            Index(name = "agp_by_contact_map", columnList = "contact_map_id,agp_id", unique = true),
        ],
    )
    val agp: List<AgpFile> = listOf(),

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "mcool_id",
        nullable = true,
    )
    val mcool: McoolFile? = null,

    @NotNull
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.DETACH],
    )
    @JoinTable(
        name = "contact_map_tracks",
        joinColumns = [
            JoinColumn(name = "contact_map_id", nullable = false)
        ],
        inverseJoinColumns = [
            JoinColumn(name = "file_id", nullable = false)
        ],
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["contact_map_id"]),
            UniqueConstraint(columnNames = ["file_id"]),
        ],
        indexes = [
            Index(name = "tracks_by_contact_map", columnList = "contact_map_id,tracks_id", unique = true),
        ],
    )
    val tracks: List<TracksFile> = listOf(),

    @NotNull
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.DETACH],
    )
    @JoinTable(
        name = "contact_map_tags",
        joinColumns = [
            JoinColumn(name = "contact_map_id", nullable = false)
        ],
        inverseJoinColumns = [
            JoinColumn(name = "tag_id", nullable = false)
        ],
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["contact_map_id"]),
            UniqueConstraint(columnNames = ["tag_id"]),
        ],
        indexes = [
            Index(name = "tags_by_contact_map", columnList = "contact_map_id,tag_id", unique = true),
        ],
    )
    val tags: List<ContactMapTag> = listOf(),

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
        mappedBy = "contactMap",
        fetch = FetchType.LAZY,
        optional = true,
        cascade = [CascadeType.DETACH],
        orphanRemoval = false,
    )
    val views: ContactMapViews? = null,
)
