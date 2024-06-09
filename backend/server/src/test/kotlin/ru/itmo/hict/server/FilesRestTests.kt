package ru.itmo.hict.server

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import ru.itmo.hict.entity.ContactMap
import ru.itmo.hict.entity.Experiment
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.controller.FilesController
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.repository.*
import ru.itmo.hict.server.service.*
import java.util.*

@WebMvcTest(FilesController::class)
@ContextConfiguration(
    classes = [FileService::class, ExperimentService::class, ContactMapService::class, FilesRestTests.RestTestBeans::class]
)
@Import(LoggerConfig::class)
class FilesRestTests {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var experimentRepository: ExperimentRepository
    @MockBean
    private lateinit var contactMapRepository: ContactMapRepository
    @MockBean
    private lateinit var minioService: MinioService
    @MockBean
    private lateinit var fileRepository: FileRepository
    @MockBean
    private lateinit var hictFileRepository: HictFileRepository
    @MockBean
    private lateinit var mcoolFileRepository: McoolFileRepository
    @MockBean
    private lateinit var agpFileRepository: AgpFileRepository
    @MockBean
    private lateinit var tracksFileRepository: TracksFileRepository
    @MockBean
    private lateinit var fastaFileRepository: FastaFileRepository

    @Test
    fun contextLoads() {
    }

    private fun expectBadRequest(vararg body: Pair<UUID, Long>) {
        body.forEach { (session, partIndex) ->
            assertDoesNotThrow {
                mvc.perform(multipart("/api/v1/files/publish")
                    .file(file)
                    .part(MockPart("session", "", "$session".toByteArray()))
                    .part(MockPart("partIndex", "", "$partIndex".toByteArray())))
                    .andExpect(status().isBadRequest)
            }
        }
    }

    private fun expectValidationException(vararg test: Pair<String, List<String>>) {
        test.forEach { (body, exceptions) ->
            mvc.perform(multipart("/api/v1/files/publish")
                .file(file)
                .part(MockPart("form", "", body.toByteArray(), MediaType.APPLICATION_JSON)))
                .andExpect {
                    Assertions.assertNotNull(it.resolvedException)
                    val err = it.resolvedException.run {
                        assert(this is ValidationException)

                        this as ValidationException
                    }.bindingResult

                    assert(err.hasErrors())
                    assert(exceptions.any { err.allErrors.first().defaultMessage!!.lowercase().contains(it.lowercase()) })
                }
        }
    }

    private infix fun String.throws(exception: String): Pair<String, List<String>> = Pair(this, listOf(exception))
    private infix fun Pair<String, List<String>>.or(exception: String) = Pair(first, second + exception)

    @Test
    fun `get all (empty)`() {
        whenever(contactMapRepository.findAll()) doReturn listOf()

        mvc.perform(get("/api/v1/hi-c/all"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"))
    }

    @Test
    fun `get all (not empty)`() {
        whenever(contactMapRepository.findAll()) doReturn listOf(contactMap)

        mvc.perform(get("/api/v1/hi-c/all"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size()", `is`(1)))
            .andExpect(jsonPath("$[0].id", `is`(2)))
            .andExpect(jsonPath("$[0].author.id", `is`(1)))
            .andExpect(jsonPath("$[0].meta.name", `is`(NAME)))
    }

    @Test
    fun `get by id (exists)`() {
        whenever(contactMapRepository.findByName("testName")) doReturn Optional.of(contactMap)

        mvc.perform(get("/api/v1/hi-c/acquire/testName"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`(2)))
            .andExpect(jsonPath("$.author.id", `is`(1)))
            .andExpect(jsonPath("$.meta.name", `is`(NAME)))
    }

    @Test
    fun `get by id (not exists)`() {
        whenever(contactMapRepository.findById(any())) doReturn Optional.empty()

        mvc.perform(get("/api/v1/hi-c/0"))
            .andExpect(status().isNotFound)
    }

    // @Nested
    // inner class Publish {
    //     private fun jsonBody(name: String?, description: String?) = """
    //         {
    //             "name": ${name?.let { "\"$it\"" }},
    //             "description": ${description?.let { "\"$it\"" }}
    //         }
    //     """.trimIndent()
    //
    //     @Test
    //     fun `valid publish`() {
    //         whenever(contactMapRepository.findByName(any())) doReturn Optional.empty()
    //         whenever(contactMapRepository.save(any<ContactMap>())) doReturn contactMap
    //
    //         mvc.perform(
    //             multipart("/api/v1/files/publish")
    //                 .file(file)
    //                 .part(form))
    //             .andExpect(status().isOk)
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.id", `is`(2)))
    //             .andExpect(jsonPath("$.author.id", `is`(1)))
    //             .andExpect(jsonPath("$.meta.name", `is`(NAME)))
    //     }
    //
    //     @Test
    //     fun `invalid registration`() {
    //         whenever(contactMapRepository.findByName(any())) doReturn Optional.of(contactMap)
    //
    //         mvc.perform(
    //             multipart("/api/v1/hi-c/publish")
    //                 .file(file)
    //                 .part(form))
    //             .andExpect {
    //                 Assertions.assertNotNull(it.resolvedException)
    //                 val err = it.resolvedException.run {
    //                     assert(this is ValidationException)
    //
    //                     this as ValidationException
    //                 }.bindingResult
    //
    //                 assert(err.hasErrors())
    //                 assert(err.allErrors.first().defaultMessage!!.lowercase().contains("must be unique"))
    //             }
    //     }
    //
    //     @Test
    //     fun `partial data to publish`() {
    //         mvc.perform(multipart("/api/v1/hi-c/publish"))
    //             .andExpect(status().isBadRequest)
    //
    //         mvc.perform(multipart("/api/v1/hi-c/publish")
    //             .file(file))
    //             .andExpect(status().isBadRequest)
    //
    //         mvc.perform(multipart("/api/v1/hi-c/publish")
    //             .part(form))
    //             .andExpect(status().isBadRequest)
    //     }
    //
    //     @Test
    //     fun `empty Hi-C creation form`() {
    //         expectBadRequest("")
    //         expectBadRequest("{}")
    //     }
    //
    //     @Test
    //     fun `invalid Hi-C creation form`() {
    //         expectBadRequest("""
    //             {
    //                 "invalid": "unsupported"
    //             }
    //         """.trimIndent())
    //     }
    //
    //     @Test
    //     fun `null field in the form`() {
    //         expectBadRequest(
    //             jsonBody(null, DESCRIPTION),
    //             jsonBody(NAME, null),
    //         )
    //     }
    //
    //     @Test
    //     fun `empty field registration form`() {
    //         expectValidationException(
    //             jsonBody("", DESCRIPTION) throws IS_BLANK or INVALID_SIZE,
    //             jsonBody(NAME, "") throws IS_BLANK or INVALID_SIZE,
    //         )
    //     }
    //
    //     @Test
    //     fun `blank field registration form`() {
    //         expectValidationException(
    //             jsonBody("    ", DESCRIPTION) throws IS_BLANK,
    //             jsonBody(NAME, "    ") throws IS_BLANK,
    //         )
    //     }
    //
    //     @Test
    //     fun `invalid length field registration form`() {
    //         expectValidationException(
    //             jsonBody("x", DESCRIPTION) throws INVALID_SIZE,
    //             jsonBody(NAME, "x") throws INVALID_SIZE,
    //         )
    //     }
    // }
    //
    // @Nested
    // inner class Attach {
    //     private fun jsonBody(name: String?, description: String?) = """
    //         {
    //             "name": ${name?.let { "\"$it\"" }},
    //             "description": ${description?.let { "\"$it\"" }}
    //         }
    //     """.trimIndent()
    //
    //     @Test
    //     fun `valid publish`() {
    //         whenever(contactMapRepository.findByName(any())) doReturn Optional.empty()
    //         whenever(contactMapRepository.save(any<ContactMap>())) doReturn contactMap
    //
    //         mvc.perform(
    //             multipart("/api/v1/files/publish")
    //                 .file(file)
    //                 .part(form))
    //             .andExpect(status().isOk)
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.id", `is`(2)))
    //             .andExpect(jsonPath("$.author.id", `is`(1)))
    //             .andExpect(jsonPath("$.meta.name", `is`(NAME)))
    //     }
    //
    //     @Test
    //     fun `invalid registration`() {
    //         whenever(contactMapRepository.findByName(any())) doReturn Optional.of(contactMap)
    //
    //         mvc.perform(
    //             multipart("/api/v1/hi-c/publish")
    //                 .file(file)
    //                 .part(form))
    //             .andExpect {
    //                 Assertions.assertNotNull(it.resolvedException)
    //                 val err = it.resolvedException.run {
    //                     assert(this is ValidationException)
    //
    //                     this as ValidationException
    //                 }.bindingResult
    //
    //                 assert(err.hasErrors())
    //                 assert(err.allErrors.first().defaultMessage!!.lowercase().contains("must be unique"))
    //             }
    //     }
    //
    //     @Test
    //     fun `partial data to publish`() {
    //         mvc.perform(multipart("/api/v1/hi-c/publish"))
    //             .andExpect(status().isBadRequest)
    //
    //         mvc.perform(multipart("/api/v1/hi-c/publish")
    //             .file(file))
    //             .andExpect(status().isBadRequest)
    //
    //         mvc.perform(multipart("/api/v1/hi-c/publish")
    //             .part(form))
    //             .andExpect(status().isBadRequest)
    //     }
    //
    //     @Test
    //     fun `empty Hi-C creation form`() {
    //         expectBadRequest("")
    //         expectBadRequest("{}")
    //     }
    //
    //     @Test
    //     fun `invalid Hi-C creation form`() {
    //         expectBadRequest("""
    //             {
    //                 "invalid": "unsupported"
    //             }
    //         """.trimIndent())
    //     }
    //
    //     @Test
    //     fun `null field in the form`() {
    //         expectBadRequest(
    //             jsonBody(null, DESCRIPTION),
    //             jsonBody(NAME, null),
    //         )
    //     }
    //
    //     @Test
    //     fun `empty field registration form`() {
    //         expectValidationException(
    //             jsonBody("", DESCRIPTION) throws IS_BLANK or INVALID_SIZE,
    //             jsonBody(NAME, "") throws IS_BLANK or INVALID_SIZE,
    //         )
    //     }
    //
    //     @Test
    //     fun `blank field registration form`() {
    //         expectValidationException(
    //             jsonBody("    ", DESCRIPTION) throws IS_BLANK,
    //             jsonBody(NAME, "    ") throws IS_BLANK,
    //         )
    //     }
    //
    //     @Test
    //     fun `invalid length field registration form`() {
    //         expectValidationException(
    //             jsonBody("x", DESCRIPTION) throws INVALID_SIZE,
    //             jsonBody(NAME, "x") throws INVALID_SIZE,
    //         )
    //     }
    // }

    @TestConfiguration
    @Profile("!full-app-test")
    class RestTestBeans {
        @Bean
        fun requestUserInfo() = RequestUserInfo("jwt", user)
    }

    private companion object {
        private const val USERNAME = "test"
        private const val LOGIN = "login"
        private const val EMAIL = "email@test.com"
        private const val PASS = "pass"

        private const val NAME = "test"
        private const val DESCRIPTION = "Example description"

        private val user = User(USERNAME, LOGIN, EMAIL, PASS, id = UUID.randomUUID())
        private val experiment = Experiment(NAME, user, Group("test"))
        private val contactMap = ContactMap(NAME, experiment, id = UUID.randomUUID())
        private val file = MockMultipartFile("file", "$NAME.hic",
            MediaType.MULTIPART_FORM_DATA_VALUE, "Example content".toByteArray())
        private val form = MockPart("form", "", """
            {
                "name": "$NAME",
                "description": "$DESCRIPTION"
            }
        """.trimIndent().toByteArray(), MediaType.APPLICATION_JSON)

        private const val IS_BLANK = "blank"
        private const val INVALID_SIZE = "size must be between"
    }
}
