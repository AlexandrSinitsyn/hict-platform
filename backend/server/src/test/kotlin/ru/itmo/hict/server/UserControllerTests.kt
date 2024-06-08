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
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.controller.UserController
import ru.itmo.hict.server.exception.UnauthorizedException
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.form.UpdatePasswordForm
import ru.itmo.hict.server.form.UpdateUserInfoForm
import ru.itmo.hict.server.logging.Logger
import ru.itmo.hict.server.service.UserService
import ru.itmo.hict.server.validator.UpdateUserInfoFormValidator
import java.sql.Timestamp
import java.util.UUID
import kotlin.random.Random

class UserControllerTests {
    private companion object {
        private val USER_ID = UUID.randomUUID()
        private val user = User(
            "username", "login", "email@test.com", "pass",
            id = USER_ID, creationTime = Timestamp(System.currentTimeMillis())
        )

        private lateinit var userController: UserController
        private lateinit var userService: UserService
        private lateinit var updateUserInfoFormValidator: UpdateUserInfoFormValidator

        private fun testRequestUserInfo() = RequestUserInfo("test-jwt", user)

        private fun noUserRequestUserInfo() = RequestUserInfo("test-jwt", null)

        private fun setCorrectRequestUserInfo() {
            userController::class.java.superclass.getDeclaredField("requestUserInfo").run {
                isAccessible = true
                set(userController, testRequestUserInfo())
            }
        }

        private fun setNullRequestUserInfo() {
            userController::class.java.superclass.getDeclaredField("requestUserInfo").run {
                isAccessible = true
                set(userController, noUserRequestUserInfo())
            }
        }

        @JvmStatic
        @BeforeAll
        fun init() {
            userService = mock<UserService>()
            updateUserInfoFormValidator = mock<UpdateUserInfoFormValidator>()
            userController = UserController(Logger("test"), userService, updateUserInfoFormValidator)

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
                                           protected val mocks: () -> Unit,
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
            } catch (e: UnauthorizedException) {
                Assertions.assertNotNull(e.message)
                Assertions.assertTrue("should be authorized" in e.message)
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
    inner class UpdateUsername : UpdateField<UpdateUserInfoForm>(
        UpdateUserInfoForm("newUsername", "newLogin", "new@email.com"),
        {
            doNothing().whenever(userService).updateUsername(any(), any())
            doNothing().whenever(userService).updateLogin(any(), any())
            doNothing().whenever(userService).updateEmail(any(), any())
        },
        userController::updateInfo,
    ) {
        @Order(2)
        @Test
        fun `correct partial update`() {
            mocks()

            fun test(form: UpdateUserInfoForm) {
                val response = method(form, bindingResult)

                Assertions.assertTrue(response.statusCode.is2xxSuccessful)
                Assertions.assertNotNull(response.body)
                Assertions.assertEquals(true, response.body!!)
            }

            test(UpdateUserInfoForm("newUsername", null, null))
            test(UpdateUserInfoForm(null, "newLogin", null))
            test(UpdateUserInfoForm(null, null, "new@email.com"))
        }

        @Order(3)
        @Test
        fun `nothing to update`() {
            mocks()

            try {
                method(UpdateUserInfoForm(null, null, null), bindingResult)
            } catch (e: ValidationException) {
                Assertions.assertNotNull(e.bindingResult)
                Assertions.assertTrue(e.bindingResult.hasErrors())
                Assertions.assertNotNull(e.bindingResult.allErrors.first().defaultMessage)
                Assertions.assertTrue("require at least one" in e.bindingResult.allErrors.first().defaultMessage!!)
            }
        }
    }

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
