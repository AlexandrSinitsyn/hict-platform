package ru.itmo.hict.server.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import ru.itmo.hict.dto.ContactMapInfoDto
import ru.itmo.hict.dto.ContactMapInfoDto.Companion.toInfoDto
import ru.itmo.hict.dto.ExperimentInfoDto
import ru.itmo.hict.dto.FileType
import ru.itmo.hict.server.exception.NoExperimentFoundException
import ru.itmo.hict.server.form.*
import ru.itmo.hict.server.logging.Logger
import ru.itmo.hict.server.service.ContactMapService
import ru.itmo.hict.server.service.GrpcContainerService
import ru.itmo.hict.server.service.MinioService
import java.util.UUID
import kotlin.io.path.*

@RestController
@RequestMapping("/api/v1/contact-map")
@CrossOrigin
class ContactMapController(
    private val contactMapService: ContactMapService,
    private val containerService: GrpcContainerService,
    private val minioService: MinioService,
    private val logger: Logger,
    @Value("\${SERVER_LOCAL_STORAGE_PATH}") private val storagePath: String,
) : ApiExceptionController() {
    @PostMapping("/new")
    fun publish(@RequestBody @Valid experimentInfoDto: ExperimentInfoDto): ResponseEntity<ContactMapInfoDto> =
        contactMapService.create(experimentInfoDto.name)?.toInfoDto()?.response()
            ?: throw NoExperimentFoundException(experimentInfoDto.name)

    @PatchMapping("/{id}/update/name")
    fun updateName(
        @PathVariable("id") id: UUID,
        @RequestBody @Valid contactMapNameUpdateForm: ContactMapNameUpdateForm,
        bindingResult: BindingResult,
    ): ResponseEntity<Boolean> = authorized { contactMapService.updateName(id, contactMapNameUpdateForm.name) }.success()

    @PatchMapping("/{id}/update/info")
    fun updateInfo(
        @PathVariable("id") id: UUID,
        @RequestBody @Valid contactMapInfoUpdateForm: ContactMapInfoUpdateForm,
        bindingResult: BindingResult,
    ): ResponseEntity<Boolean> = authorized {
        contactMapService.updateInfo(id, contactMapInfoUpdateForm.description, contactMapInfoUpdateForm.link)
    }.success()

    @GetMapping("/acquire/{name}/ping")
    fun ping(@PathVariable("name") name: String): ResponseEntity<Boolean> =
        authorized { containerService.ping(this) }.success()

    @GetMapping("/acquire/{name}")
    fun getByName(@PathVariable("name") name: String): ResponseEntity<ContactMapInfoDto> = authorized {
        val contactMap = contactMapService.getByName(name)
            ?: return@authorized ResponseEntity.notFound().build()

        contactMapService.view(contactMap)

        logger.info("request-map", name, "viewed")

        contactMap.hict?.let {
            val filename = "${it.file.id!!}.${FileType.HICT.extension}"

            val alreadySavedFile = Path(storagePath).listDirectoryEntries().find { f -> f.name == filename }

            if (alreadySavedFile == null) {
                logger.warn("search-file", filename, "not found")

                minioService.downloadFile(FileType.HICT.bucket, filename)
                    .data.transferTo(Path(storagePath, filename).outputStream())
            } else {
                logger.info("search-file", filename, "found")
            }
        }

        containerService.publish(this)

        contactMap.toInfoDto().response()
    }
}
