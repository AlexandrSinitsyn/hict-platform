package ru.itmo.hict.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

@Entity
@Table(name = "hi_c_maps")
class HiCMap(
    @NotNull                                                 @ManyToOne val author: User,
    @NotNull @NotBlank                                                  val name: String,
    @NotNull @NotBlank @Column(columnDefinition = "TEXT")          @Lob val description: String,
    @NotNull @GeneratedValue(strategy = GenerationType.IDENTITY)    @Id val id: Long? = null,
    @NotNull                                         @CreationTimestamp val creationTime: Timestamp? = null,
)
