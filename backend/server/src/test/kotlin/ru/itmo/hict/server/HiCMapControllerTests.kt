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
import org.springframework.mock.web.MockMultipartFile
import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import org.springframework.web.multipart.MultipartFile
import ru.itmo.hict.entity.HiCMap
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.controller.HiCMapController
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.HiCMapCreationForm
import ru.itmo.hict.server.service.HiCMapService
import ru.itmo.hict.server.validator.HiCMapCreationFormValidator
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Timestamp
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

class HiCMapControllerTests {
    private companion object {
        private const val USER_ID = 1L
        private const val ID = 2L
        private val user = User(
            "username", "login", "email@test.com", "pass", Role.USER,
            id = USER_ID, creationTime = Timestamp(System.currentTimeMillis())
        )
        private val hicMap = HiCMap(user, "name", "description",
            id = ID, creationTime = Timestamp(System.currentTimeMillis()))

        private lateinit var hicController: HiCMapController
        private lateinit var hicService: HiCMapService
        private lateinit var creationValidator: HiCMapCreationFormValidator

        private val TEMP_PATH = Files.createTempDirectory("hic_controller_tests")

        private fun testRequestUserInfo() = RequestUserInfo("test-jwt", user)

        private fun noUserRequestUserInfo() = RequestUserInfo("test-jwt", null)

        private fun setCorrectRequestUserInfo() {
            hicController::class.java.getDeclaredField("requestUserInfo").run {
                isAccessible = true
                set(hicController, testRequestUserInfo())
            }
        }

        private fun setNullRequestUserInfo() {
            hicController::class.java.getDeclaredField("requestUserInfo").run {
                isAccessible = true
                set(hicController, noUserRequestUserInfo())
            }
        }

        @JvmStatic
        @BeforeAll
        fun init() {
            hicService = mock<HiCMapService>()
            creationValidator = mock<HiCMapCreationFormValidator>()
            hicController = HiCMapController(hicService, creationValidator)

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
        whenever(hicService.getById(ID)) doReturn hicMap

        val response = hicController.getById(ID)

        Assertions.assertTrue(response.statusCode.is2xxSuccessful)
        Assertions.assertNotNull(response.body)
        Assertions.assertEquals(ID, response.body!!.id)
    }

    @Test
    fun `get invalid by id`() {
        whenever(hicService.getById(any())) doReturn null

        val response = hicController.getById(ID)

        Assertions.assertTrue(response.statusCode.is4xxClientError)
        Assertions.assertNull(response.body)
    }

    @TestMethodOrder(OrderAnnotation::class)
    @Nested
    inner class Publish {
        private val fileName = "test.hic"
        private val content = "Hello, World!".toByteArray()
        private val file: MultipartFile = MockMultipartFile(fileName, content)
        private val form = HiCMapCreationForm("name", "description")
        private val bindingResult: BindingResult = DirectFieldBindingResult(this, "test")

        @Order(1)
        @Test
        fun `correct publish`() {
            whenever(hicService.load(any(), any(), any(), any())) doReturn hicMap

            val response = hicController.publish(file, form, bindingResult)

            Assertions.assertTrue(response.statusCode.is2xxSuccessful)
            Assertions.assertNotNull(response.body)
            Assertions.assertEquals(ID, response.body!!.id)
        }

        @Order(1)
        @Test
        fun `not authorized`() {
            setNullRequestUserInfo()

            try {
                hicController.publish(file, form, bindingResult)
            } catch (e: ValidationException) {
                Assertions.assertNotNull(e.bindingResult)
                Assertions.assertTrue(e.bindingResult.hasErrors())
                Assertions.assertNotNull(e.bindingResult.allErrors.first().defaultMessage)
                Assertions.assertTrue("must be authorized" in e.bindingResult.allErrors.first().defaultMessage!!)
            } finally {
                setCorrectRequestUserInfo()
            }
        }

        @Order(1)
        @Test
        fun `empty file`() {
            try {
                hicController.publish(MockMultipartFile("invalid.hic", ByteArray(0)), form, bindingResult)
            } catch (e: ValidationException) {
                Assertions.assertNotNull(e.bindingResult)
                Assertions.assertTrue(e.bindingResult.hasErrors())
                Assertions.assertNotNull(e.bindingResult.allErrors.first().defaultMessage)
                Assertions.assertTrue("should not be empty" in e.bindingResult.allErrors.first().defaultMessage!!)
            }
        }

        @Order(2)
        @Test
        fun `has validation errors`() {
            Assertions.assertThrows(ValidationException::class.java) {
                bindingResult.reject("some-error", "Default test error")
                hicController.publish(file, form, bindingResult)
            }
        }
    }
}
