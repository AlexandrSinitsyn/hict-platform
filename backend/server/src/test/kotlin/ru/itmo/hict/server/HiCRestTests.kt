package ru.itmo.hict.server

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import ru.itmo.hict.entity.HiCMap
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.controller.HiCMapController
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.repository.HiCMapRepository
import ru.itmo.hict.server.service.*
import ru.itmo.hict.server.validator.HiCMapCreationFormValidator
import java.nio.file.Files
import java.sql.Timestamp
import java.util.*

@WebMvcTest(HiCMapController::class)
@ContextConfiguration(
    classes = [HiCMapController::class, HiCMapService::class, FileService::class, HiCMapCreationFormValidator::class,
        HiCRestTests.RestTestBeans::class]
)
class HiCRestTests {
    @Autowired
    private lateinit var mvc: MockMvc
    @Autowired
    private lateinit var fileService: FileService

    @MockBean
    private lateinit var hiCMapRepository: HiCMapRepository
    @MockBean
    private lateinit var minioService: MinioService

    @PostConstruct
    fun setup() {
        Assertions.assertTrue(Files.list(fileService.tmp(".")).count() == 0L)
        Assertions.assertTrue(Files.list(fileService.minio(".")).count() == 0L)

        doNothing().whenever(minioService).newBucketIfAbsent(any())
        doNothing().whenever(minioService).upload(any(), any(), any())
    }

    @PreDestroy
    fun destructor() {
        Assertions.assertTrue(Files.list(fileService.tmp(".")).count() == 0L)
        Assertions.assertTrue(Files.list(fileService.minio(".")).count() == 0L)
    }

    @Test
    fun contextLoads() {
    }

    private fun expectBadRequest(vararg body: String) {
        body.forEach {
            assertDoesNotThrow {
                mvc.perform(multipart("/api/v1/hi-c/publish")
                    .file(file)
                    .part(MockPart("form", "", it.toByteArray(), MediaType.APPLICATION_JSON)))
                    .andExpect(status().isBadRequest)
            }
        }
    }

    private fun expectValidationException(vararg test: Pair<String, List<String>>) {
        test.forEach { (body, exceptions) ->
            mvc.perform(multipart("/api/v1/hi-c/publish")
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
        whenever(hiCMapRepository.findAll()) doReturn listOf()

        mvc.perform(get("/api/v1/hi-c/all"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"))
    }

    @Test
    fun `get all (not empty)`() {
        whenever(hiCMapRepository.findAll()) doReturn listOf(hicMap)

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
        whenever(hiCMapRepository.findById(0)) doReturn Optional.of(hicMap)

        mvc.perform(get("/api/v1/hi-c/0"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`(2)))
            .andExpect(jsonPath("$.author.id", `is`(1)))
            .andExpect(jsonPath("$.meta.name", `is`(NAME)))
    }

    @Test
    fun `get by id (not exists)`() {
        whenever(hiCMapRepository.findById(any())) doReturn Optional.empty()

        mvc.perform(get("/api/v1/hi-c/0"))
            .andExpect(status().isNotFound)
    }

    @Nested
    inner class Publish {
        private fun jsonBody(name: String?, description: String?) = """
            {
                "name": ${name?.let { "\"$it\"" }},
                "description": ${description?.let { "\"$it\"" }}
            }
        """.trimIndent()

        @Test
        fun `valid publish`() {
            whenever(hiCMapRepository.findByName(any())) doReturn Optional.empty()
            whenever(hiCMapRepository.save(any<HiCMap>())) doReturn hicMap

            mvc.perform(
                multipart("/api/v1/hi-c/publish")
                    .file(file)
                    .part(form))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", `is`(2)))
                .andExpect(jsonPath("$.author.id", `is`(1)))
                .andExpect(jsonPath("$.meta.name", `is`(NAME)))
        }

        @Test
        fun `invalid registration`() {
            whenever(hiCMapRepository.findByName(any())) doReturn Optional.of(hicMap)

            mvc.perform(
                multipart("/api/v1/hi-c/publish")
                    .file(file)
                    .part(form))
                .andExpect {
                    Assertions.assertNotNull(it.resolvedException)
                    val err = it.resolvedException.run {
                        assert(this is ValidationException)

                        this as ValidationException
                    }.bindingResult

                    assert(err.hasErrors())
                    assert(err.allErrors.first().defaultMessage!!.lowercase().contains("must be unique"))
                }
        }

        @Test
        fun `partial data to publish`() {
            mvc.perform(multipart("/api/v1/hi-c/publish"))
                .andExpect(status().isBadRequest)

            mvc.perform(multipart("/api/v1/hi-c/publish")
                .file(file))
                .andExpect(status().isBadRequest)

            mvc.perform(multipart("/api/v1/hi-c/publish")
                .part(form))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `empty Hi-C creation form`() {
            expectBadRequest("")
            expectBadRequest("{}")
        }

        @Test
        fun `invalid Hi-C creation form`() {
            expectBadRequest("""
                {
                    "invalid": "unsupported"
                }
            """.trimIndent())
        }

        @Test
        fun `null field in the form`() {
            expectBadRequest(
                jsonBody(null, DESCRIPTION),
                jsonBody(NAME, null),
            )
        }

        @Test
        fun `empty field registration form`() {
            expectValidationException(
                jsonBody("", DESCRIPTION) throws IS_BLANK or INVALID_SIZE,
                jsonBody(NAME, "") throws IS_BLANK or INVALID_SIZE,
            )
        }

        @Test
        fun `blank field registration form`() {
            expectValidationException(
                jsonBody("    ", DESCRIPTION) throws IS_BLANK,
                jsonBody(NAME, "    ") throws IS_BLANK,
            )
        }

        @Test
        fun `invalid length field registration form`() {
            expectValidationException(
                jsonBody("x", DESCRIPTION) throws INVALID_SIZE,
                jsonBody(NAME, "x") throws INVALID_SIZE,
            )
        }
    }

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

        private val user =
            User(USERNAME, LOGIN, EMAIL, PASS, Role.ANONYMOUS, 1, Timestamp(System.currentTimeMillis()))
        private val hicMap = HiCMap(user, NAME, DESCRIPTION, 2, Timestamp(System.currentTimeMillis()))
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
