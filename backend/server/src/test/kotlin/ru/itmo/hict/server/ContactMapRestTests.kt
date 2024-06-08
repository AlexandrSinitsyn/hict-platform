package ru.itmo.hict.server

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
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import ru.itmo.hict.entity.ContactMap
import ru.itmo.hict.entity.Experiment
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.controller.ContactMapController
import ru.itmo.hict.server.exception.NoExperimentFoundException
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.repository.*
import ru.itmo.hict.server.service.*
import java.util.*

@EnableAspectJAutoProxy
@WebMvcTest(ContactMapController::class)
@ContextConfiguration(
    classes = [ContactMapService::class, ContactMapRestTests.RestTestBeans::class]
)
@Import(LoggerConfig::class)
class ContactMapRestTests {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var experimentService: ExperimentService
    @MockBean
    private lateinit var contactMapRepository: ContactMapRepository
    @MockBean
    private lateinit var viewsRepository: ViewsRepository
    @MockBean
    private lateinit var minioService: MinioService
    @MockBean
    private lateinit var fileService: FileService
    @MockBean
    private lateinit var containerService: GrpcContainerService

    @Test
    fun contextLoads() {
    }

    // @Test
    // fun `get by id (exists)`() {
    //     doNothing().whenever(viewsRepository.viewById(any()))
    //     doNothing().whenever(minioService.downloadFile(any(), any()))
    //     doNothing().whenever(containerService.publish(any()))
    //     whenever(contactMapRepository.findByName("testName")) doReturn Optional.of(contactMap)
    //
    //     mvc.perform(get("/api/v1/contact-map/acquire/testName"))
    //         .andExpect(status().isOk)
    //         .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //         .andExpect(jsonPath("$.id", `is`(2)))
    //         .andExpect(jsonPath("$.author.id", `is`(1)))
    //         .andExpect(jsonPath("$.meta.name", `is`(NAME)))
    // }

    @Test
    fun `get by id (not exists)`() {
        whenever(contactMapRepository.findById(any())) doReturn Optional.empty()

        mvc.perform(get("/api/v1/contact-map/unknown"))
            .andExpect(status().isNotFound)
    }

    @Nested
    inner class New {
        private fun expectBadRequest(vararg body: String) {
            body.forEach {
                assertDoesNotThrow {
                    mvc.perform(post("/api/v1/contact-map/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it))
                        .andExpect(status().isBadRequest)
                }
            }
        }

        private fun expectValidationException(vararg test: Pair<String, List<String>>) {
            test.forEach { (body, exceptions) ->
                mvc.perform(post("/api/v1/contact-map/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
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

        private fun jsonBody(experimentId: String?) = """
            {
                "experimentId": ${experimentId?.let { "\"$it\"" }}
            }
        """.trimIndent()

        @Test
        fun `valid publish`() {
            whenever(experimentService.getById(EXPERIMENT_ID)) doReturn experiment
            whenever(contactMapRepository.save(any<ContactMap>())) doReturn contactMap

            mvc.perform(
                post("/api/v1/contact-map/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody("$EXPERIMENT_ID")))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", `is`(CONTACT_MAP_ID)))
                .andExpect(jsonPath("$.name", `is`(NAME)))
        }

        @Test
        fun `invalid experiment`() {
            whenever(experimentService.getById(any())) doReturn null

            mvc.perform(post("/api/v1/contact-map/new"))
                .andExpect {
                    Assertions.assertNotNull(it.resolvedException)
                    val err = it.resolvedException.run {
                        assert(this is NoExperimentFoundException)

                        this as NoExperimentFoundException
                    }.message

                    assert(err.lowercase().contains("no such experiment"))
                }
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
                jsonBody(null),
            )
        }

        @Test
        fun `empty field registration form`() {
            expectValidationException(
                jsonBody("") throws IS_BLANK or INVALID_SIZE,
            )
        }

        @Test
        fun `blank field registration form`() {
            expectValidationException(
                jsonBody("    ") throws IS_BLANK,
            )
        }

        @Test
        fun `invalid length field registration form`() {
            expectValidationException(
                jsonBody("x") throws INVALID_SIZE,
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

        private val EXPERIMENT_ID = UUID.randomUUID()
        private val CONTACT_MAP_ID = UUID.randomUUID()
        private const val NAME = "test"

        private val user = User(USERNAME, LOGIN, EMAIL, PASS, id = UUID.randomUUID())
        private val experiment = Experiment(NAME, user, Group("test"), id = EXPERIMENT_ID)
        private val contactMap = ContactMap(NAME, experiment, id = CONTACT_MAP_ID)

        private const val IS_BLANK = "blank"
        private const val INVALID_SIZE = "size must be between"
    }
}
