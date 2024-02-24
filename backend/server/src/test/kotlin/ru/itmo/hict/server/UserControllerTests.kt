package ru.itmo.hict.server

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.*
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.controller.UserController
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.UpdateEmailForm
import ru.itmo.hict.server.form.UpdateLoginForm
import ru.itmo.hict.server.form.UpdatePasswordForm
import ru.itmo.hict.server.form.UpdateUsernameForm
import ru.itmo.hict.server.service.UserService
import java.sql.Timestamp
import kotlin.random.Random

class UserControllerTests {
    private companion object {
        private const val USER_ID = 1L
        private val user = User(
            "username", "login", "email@test.com", "pass", Role.USER,
            id = USER_ID, creationTime = Timestamp(System.currentTimeMillis())
        )

        private lateinit var userController: UserController
        private lateinit var userService: UserService

        private fun testRequestUserInfo() = RequestUserInfo("test-jwt", user)

        private fun noUserRequestUserInfo() = RequestUserInfo("test-jwt", null)

        private fun setCorrectRequestUserInfo() {
            userController::class.java.getDeclaredField("requestUserInfo").run {
                isAccessible = true
                set(userController, testRequestUserInfo())
            }
        }

        private fun setNullRequestUserInfo() {
            userController::class.java.getDeclaredField("requestUserInfo").run {
                isAccessible = true
                set(userController, noUserRequestUserInfo())
            }
        }

        @JvmStatic
        @BeforeAll
        fun init() {
            userService = mock<UserService>()
            userController = UserController(userService)

            setCorrectRequestUserInfo()
        }
    }

    @Test
    fun `get existing by id`() {
        val count = Random.nextLong()
        whenever(userService.count()) doReturn count

        val response = userController.count()

        Assertions.assertTrue(response.statusCode.is2xxSuccessful)
        Assertions.assertNotNull(response.body)
        Assertions.assertEquals(count, response.body!!)
    }

    @TestMethodOrder(OrderAnnotation::class)
    abstract inner class UpdateField<Form>(protected val form: Form,
                                           private val mocks: () -> Unit,
                                           protected val method: (Form, BindingResult) -> ResponseEntity<Boolean>) {
        protected val bindingResult: BindingResult = DirectFieldBindingResult(this, "test")

        @Order(1)
        @Test
        fun `correct update`() {
            mocks()

            val response = method(form, bindingResult)

            Assertions.assertTrue(response.statusCode.is2xxSuccessful)
            Assertions.assertNotNull(response.body)
            Assertions.assertEquals(true, response.body!!)
        }

        @Order(1)
        @Test
        fun `not authorized`() {
            setNullRequestUserInfo()

            try {
                method(form, bindingResult)
            } catch (e: ValidationException) {
                Assertions.assertNotNull(e.bindingResult)
                Assertions.assertTrue(e.bindingResult.hasErrors())
                Assertions.assertNotNull(e.bindingResult.allErrors.first().defaultMessage)
                Assertions.assertTrue("must be authorized" in e.bindingResult.allErrors.first().defaultMessage!!)
            } finally {
                setCorrectRequestUserInfo()
            }
        }

        @Test
        fun `has validation errors`() {
            Assertions.assertThrows(ValidationException::class.java) {
                bindingResult.reject("some-error", "Default test error")
                method(form, bindingResult)
            }
        }
    }

    @Nested
    inner class UpdateUsername : UpdateField<UpdateUsernameForm>(
        UpdateUsernameForm("newUsername"),
        { whenever(userService.updateUsername(any(), any())) doReturn true },
        userController::updateUsername,
    )

    @Nested
    inner class UpdateLogin : UpdateField<UpdateLoginForm>(
        UpdateLoginForm("newLogin"),
        { whenever(userService.updateLogin(any(), any())) doReturn true },
        userController::updateLogin,
    )

    @Nested
    inner class UpdateEmail : UpdateField<UpdateEmailForm>(
        UpdateEmailForm("new@email.com"),
        { whenever(userService.updateEmail(any(), any())) doReturn true },
        userController::updateEmail,
    )

    @Nested
    inner class UpdatePassword : UpdateField<UpdatePasswordForm>(
        UpdatePasswordForm("pass", "newPassword"),
        {
            whenever(userService.updatePassword(any(), any(), any())) doReturn true
            whenever(userService.checkCredentials(any(), any())) doReturn true
        },
        userController::updatePassword,
    ) {
        @Order(2)
        @Test
        fun `invalid old password`() {
            whenever(userService.updatePassword(any(), any(), any())) doReturn false
            whenever(userService.checkCredentials(any(), any())) doReturn false

            try {
                method(form, bindingResult)
            } catch (e: ValidationException) {
                Assertions.assertNotNull(e.bindingResult)
                Assertions.assertTrue(e.bindingResult.hasErrors())
                Assertions.assertNotNull(e.bindingResult.allErrors.first().defaultMessage)
                Assertions.assertTrue("must confirm" in e.bindingResult.allErrors.first().defaultMessage!!)
            }
        }
    }
}
