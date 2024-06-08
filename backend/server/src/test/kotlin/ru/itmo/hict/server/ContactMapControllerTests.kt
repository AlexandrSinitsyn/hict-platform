package ru.itmo.hict.server

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.*
import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import ru.itmo.hict.entity.ContactMap
import ru.itmo.hict.entity.Experiment
import ru.itmo.hict.entity.Group
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.controller.ContactMapController
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.ContactMapCreationForm
import ru.itmo.hict.server.logging.Logger
import ru.itmo.hict.server.service.ContactMapService
import ru.itmo.hict.server.service.GrpcContainerService
import ru.itmo.hict.server.service.MinioService
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString

class ContactMapControllerTests {
    private companion object {
        private val USER_ID = UUID.randomUUID()
        private val user = User(
            "username", "login", "email@test.com", "pass",
            id = USER_ID
        )
        private val EXPERIMENT_ID = UUID.randomUUID()
        private val experiment = Experiment(
            "test-experiment", user, Group("test-group"),
            id = EXPERIMENT_ID
        )
        private val ID = UUID.randomUUID()
        private const val NAME = "name"
        private val contactMap = ContactMap(NAME, experiment, id = ID)

        private lateinit var contactMapController: ContactMapController
        private lateinit var contactMapService: ContactMapService
        private lateinit var containerService: GrpcContainerService
        private lateinit var minioService: MinioService

        private val TEMP_PATH = Files.createTempDirectory("hic_controller_tests")

        private fun testRequestUserInfo() = RequestUserInfo("test-jwt", user)

        private fun noUserRequestUserInfo() = RequestUserInfo("test-jwt", null)

        private fun setCorrectRequestUserInfo() {
            contactMapController::class.java.getDeclaredField("requestUserInfo").run {
                isAccessible = true
                set(contactMapController, testRequestUserInfo())
            }
        }

        private fun setNullRequestUserInfo() {
            contactMapController::class.java.getDeclaredField("requestUserInfo").run {
                isAccessible = true
                set(contactMapController, noUserRequestUserInfo())
            }
        }

        @JvmStatic
        @BeforeAll
        fun init() {
            contactMapService = mock<ContactMapService>()
            contactMapController =
                ContactMapController(contactMapService, containerService, minioService, Logger("test"), TEMP_PATH.pathString)

            setCorrectRequestUserInfo()
        }

        @JvmStatic
        @BeforeAll
        fun `create temporary directory`() {
            Files.createDirectories(TEMP_PATH)
        }

        @JvmStatic
        @AfterAll
        fun `destroy temporary directory`() {
            fun dfs(path: Path) {
                if (path.isDirectory()) {
                    path.listDirectoryEntries().forEach { dfs(it) }
                }

                println("> ${path.fileName}")
                Files.deleteIfExists(path)
            }

            dfs(TEMP_PATH)
        }
    }

    @Test
    fun `get existing by id`() {
        whenever(contactMapService.getByName(NAME)) doReturn contactMap

        val response = contactMapController.getByName(NAME)

        Assertions.assertTrue(response.statusCode.is2xxSuccessful)
        Assertions.assertNotNull(response.body)
        Assertions.assertEquals(ID, response.body!!.id)
    }

    @Test
    fun `get invalid by id`() {
        whenever(contactMapService.getByName(any())) doReturn null

        val response = contactMapController.getByName(NAME)

        Assertions.assertTrue(response.statusCode.is4xxClientError)
        Assertions.assertNull(response.body)
    }

    @Suppress("ClassName")
    @TestMethodOrder(OrderAnnotation::class)
    @Nested
    inner class `Publish and Ping` {
        private val form = ContactMapCreationForm(EXPERIMENT_ID)
        private val bindingResult: BindingResult = DirectFieldBindingResult(this, "test")

        @Order(1)
        @Test
        fun `correct publish`() {
            whenever(contactMapService.create(any())) doReturn contactMap

            val response = contactMapController.publish(form, bindingResult)

            Assertions.assertTrue(response.statusCode.is2xxSuccessful)
            Assertions.assertNotNull(response.body)
            Assertions.assertEquals(ID, response.body!!.id)
        }

        @Order(1)
        @Test
        fun `incorrect publish`() {
            val response = contactMapController.publish(ContactMapCreationForm(UUID.randomUUID()), bindingResult)

            Assertions.assertTrue(response.statusCode.is2xxSuccessful)
            Assertions.assertNotNull(response.body)
            Assertions.assertEquals(ID, response.body!!.id)
        }

        @Order(2)
        @Test
        fun `authorized ping`() {
            doNothing().whenever(containerService.ping(any()))

            val response = contactMapController.ping(NAME)

            Assertions.assertTrue(response.statusCode.is2xxSuccessful)
            Assertions.assertNotNull(response.body)
            Assertions.assertEquals(true, response.body!!)
        }

        @Order(2)
        @Test
        fun `unauthorized ping`() {
            setNullRequestUserInfo()

            try {
                contactMapController.ping(NAME)
            } catch (e: ValidationException) {
                Assertions.assertNotNull(e.bindingResult)
                Assertions.assertTrue(e.bindingResult.hasErrors())
                Assertions.assertNotNull(e.bindingResult.allErrors.first().defaultMessage)
                Assertions.assertTrue("must be authorized" in e.bindingResult.allErrors.first().defaultMessage!!)
            } finally {
                setCorrectRequestUserInfo()
            }
        }

        @Order(3)
        @Test
        fun `has validation errors`() {
            Assertions.assertThrows(ValidationException::class.java) {
                bindingResult.reject("some-error", "Default test error")
                contactMapController.publish(form, bindingResult)
            }
        }
    }
}
