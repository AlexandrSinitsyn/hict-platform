package ru.itmo.hict.entity

import jakarta.annotation.Nullable
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

enum class SequenceLevelType {
    CONTIG,
    SCAFFOLD,
    CHROMOSOME,
}

sealed interface AttachedFile {
    val file: File
}

@Entity
@Table(
    name = "files",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "file_by_id", columnList = "file_id", unique = true),
        Index(name = "file_by_filename", columnList = "filename,file_id", unique = true),
        Index(name = "file_by_group", columnList = "sequence_level,file_id", unique = true),
        Index(name = "file_by_sequence_level", columnList = "visibility_group,file_id", unique = true),
    ],
)
class File(
    @NotNull
    @NotBlank
    @Size(max = 256)
    @Column(name = "filename", nullable = false)
    val filename: String,

    @NotNull
    @NotBlank
    @Column(name = "sequence_level", columnDefinition = "VARCHAR", nullable = false)
    @Enumerated(EnumType.STRING)
    val sequenceLevel: SequenceLevelType,

    @NotNull
    @NotBlank
    @Column(name = "file_size", nullable = false)
    val fileSize: Long,

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "visibility_group",
        nullable = false,
    )
    val visibilityGroup: Group,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "file_id", nullable = false)
    val id: Long? = null,

    @NotNull
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false)
    val creationTime: Timestamp? = null,
)

@Entity
@Table(
    name = "files_hic",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_hic_by_id", columnList = "hic_id", unique = true),
    ],
)
class HiCFile(
    @NotNull
    @Id
    @Column(name = "file_id", insertable = false, updatable = false)
    private val fileId: Long,

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    override val file: File,

    @Nullable
    @Column(name = "min_resolutions", nullable = true)
    val minResolutions: Long? = null,

    @Nullable
    @Column(name = "max_resolutions", nullable = true)
    val maxResolutions: Long? = null,
) : AttachedFile

@Entity
@Table(
    name = "files_mcool",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_mcool_by_id", columnList = "mcool_id", unique = true),
    ],
)
class McoolFile(
    @NotNull
    @Id
    @Column(name = "file_id", insertable = false, updatable = false)
    private val fileId: Long,

    @NotNull
    @NotBlank
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    override val file: File,

    @Nullable
    @Column(name = "min_resolutions", nullable = true)
    val minResolutions: Long? = null,

    @Nullable
    @Column(name = "max_resolutions", nullable = true)
    val maxResolutions: Long? = null,
) : AttachedFile

@Entity
@Table(
    name = "files_agp",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_agp_by_id", columnList = "agp_id", unique = true),
    ],
)
class AgpFile(
    @NotNull
    @Id
    @Column(name = "file_id", insertable = false, updatable = false)
    private val fileId: Long,

    @NotNull
    @NotBlank
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    override val file: File,
) : AttachedFile

@Entity
@Table(
    name = "files_tracks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_tracks_by_id", columnList = "tracks_id", unique = true),
    ],
)
class TracksFile(
    @NotNull
    @Id
    @Column(name = "file_id", insertable = false, updatable = false)
    private val fileId: Long,

    @NotNull
    @NotBlank
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    override val file: File,

    @Nullable
    @NotBlankIfPresent
    @Size(min = 3, max = 100)
    @Column(name = "data_source", nullable = true)
    val dataSource: String? = null,

    @Nullable
    @NotBlankIfPresent
    @Size(max = 65536)
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    val description: String? = null,

    @Nullable
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "tracks_type_id",
        nullable = true,
    )
    val tracksTypes: TracksTypes? = null,
) : AttachedFile

@Entity
@Table(
    name = "tracks_types",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["tracks_type_id"]),
        UniqueConstraint(columnNames = ["tracks_type_name"]),
    ],
    indexes = [
        Index(name = "tracks_types_by_id", columnList = "tracks_type_id", unique = true),
        Index(name = "tracks_types_by_name", columnList = "tracks_type_name,tracks_type_id", unique = true),
    ],
)
class TracksTypes(
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "tracks_type_id", nullable = false)
    val id: Long? = null,

    @NotNull
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "tracks_type_name", nullable = false)
    val name: String,
)

@Entity
@Table(
    name = "files_fasta",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_fasta_by_id", columnList = "fasta_id", unique = true),
    ],
)
class FastaFile(
    @NotNull
    @Id
    @Column(name = "file_id", insertable = false, updatable = false)
    private val fileId: Long,

    @NotNull
    @NotBlank
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    override val file: File,
) : AttachedFile
