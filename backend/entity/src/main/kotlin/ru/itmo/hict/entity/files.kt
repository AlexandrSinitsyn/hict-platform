package ru.itmo.hict.entity

import jakarta.annotation.Nullable
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import java.io.Serializable
import java.sql.Timestamp

enum class SequenceLevelType {
    CONTIG,
    SCAFFOLD,
    CHROMOSOME,
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
    @Column(name = "sequence_level", nullable = false)
    val sequenceLevel: SequenceLevelType,

    @NotNull
    @NotBlank
    @Column(name = "file_size", nullable = false)
    val fileSize: String,

    @NotNull
    @Column(name = "public", nullable = false)
    val public: Boolean = false,

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "visibility_group",
        nullable = true,
    )
    val visibilityGroup: Group? = null,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "file_id", nullable = false)
    val id: Long? = null,
)

@Entity
@Table(
    name = "files_hict",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["hict_id"]),
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_hict_by_id", columnList = "hict_id", unique = true),
    ],
)
class HiCTFile(
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    val file: File,

    @Nullable
    @Column(name = "min_resolutions", nullable = true)
    val minResolutions: Long? = null,

    @Nullable
    @Column(name = "max_resolutions", nullable = true)
    val maxResolutions: Long? = null,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "hict_id", nullable = false)
    val id: Long? = null,
)

@Entity
@Table(
    name = "files_tracks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["tracks_id"]),
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_tracks_by_id", columnList = "tracks_id", unique = true),
    ],
)
class TracksFile(
    @NotNull
    @NotBlank
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    val file: File,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "tracks_id", nullable = false)
    val id: Long? = null,
)

@Entity
@Table(
    name = "files_mcool",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["mcool_id"]),
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_mcool_by_id", columnList = "mcool_id", unique = true),
    ],
)
class McoolFile(
    @NotNull
    @NotBlank
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    val file: File,

    @Nullable
    @Column(name = "min_resolutions", nullable = true)
    val minResolutions: Long? = null,

    @Nullable
    @Column(name = "max_resolutions", nullable = true)
    val maxResolutions: Long? = null,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "mcool_id", nullable = false)
    val id: Long? = null,
)

@Entity
@Table(
    name = "files_agp",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["agp_id"]),
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_agp_by_id", columnList = "agp_id", unique = true),
    ],
)
class AgpFile(
    @NotNull
    @NotBlank
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    val file: File,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "agp_id", nullable = false)
    val id: Long? = null,
)

@Entity
@Table(
    name = "files_fasta",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["fasta_id"]),
        UniqueConstraint(columnNames = ["file_id"]),
    ],
    indexes = [
        Index(name = "files_fasta_by_id", columnList = "fasta_id", unique = true),
    ],
)
class FastaFile(
    @NotNull
    @NotBlank
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
    @JoinColumn(
        name = "file_id",
        nullable = false,
    )
    val file: File,

    @NotNull
    @Column(name = "draft", nullable = false)
    val draft: Boolean,

    @NotNull
    @Column(name = "scaffolded", nullable = false)
    val scaffolded: Boolean,

    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "fasta_id", nullable = false)
    val id: Long? = null,
)
