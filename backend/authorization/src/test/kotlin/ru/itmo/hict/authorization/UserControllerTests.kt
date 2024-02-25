package ru.itmo.hict.authorization

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.*
import org.junit.jupiter.api.function.ThrowingSupplier
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.DirectFieldBindingResult
import ru.itmo.hict.authorization.controller.UserController
import ru.itmo.hict.authorization.exception.ValidationException
import ru.itmo.hict.authorization.form.EnterForm
import ru.itmo.hict.authorization.form.RegisterForm
import ru.itmo.hict.authorization.service.JwtService
import ru.itmo.hict.authorization.service.UserService
import ru.itmo.hict.authorization.validator.EnterFormValidator
import ru.itmo.hict.authorization.validator.RegisterFormValidator
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import java.util.*

class UserControllerTests {
    private lateinit var jwtService: JwtService
    private lateinit var userService: UserService
    private lateinit var userController: UserController

    @BeforeEach
    fun setup() {
        jwtService = mock<JwtService>()
        userService = mock<UserService>()
        userController =
            UserController(userService, jwtService, mock<RegisterFormValidator>(), mock<EnterFormValidator>())
    }

    @Test
    fun initialized() {
        Assertions.assertNotNull(userController)
    }

    @Test
    fun `correct registration`() {
        whenever(userService.register(USERNAME, LOGIN, EMAIL, PASS)).thenReturn(user)
        whenever(jwtService.create(user)).thenReturn("jwt")

        val registerForm = RegisterForm(USERNAME, LOGIN, EMAIL, PASS)

        val bindingResult: BindingResult = DirectFieldBindingResult(this, "test")

        val response = Assertions.assertDoesNotThrow(ThrowingSupplier {
            userController.register(registerForm, bindingResult)
        })

        assert(!bindingResult.hasErrors())

        assert(response.statusCode == HttpStatus.OK)
        Assertions.assertNotNull(response.body)

        val jwt = response.body!!
        assert(jwt.isNotBlank())
    }

    @Test
    fun `invalid registration form`() {
        val registerForm = RegisterForm(INVALID, LOGIN, EMAIL, PASS)

        val bindingResult: BindingResult = DirectFieldBindingResult(this, "test")
        bindingResult.reject("invalid-username", "test")

        assertThrows<ValidationException> {
            userController.register(registerForm, bindingResult)
        }

        assert(bindingResult.hasErrors())
    }

    @Test
    fun `duplicated user`() {
        whenever(userService.register(USERNAME, LOGIN, EMAIL, PASS)).thenReturn(null)

        val registerForm = RegisterForm(USERNAME, LOGIN, EMAIL, PASS)

        val bindingResult: BindingResult = DirectFieldBindingResult(this, "test")

        assert(!bindingResult.hasErrors())

        assertThrows<ValidationException> {
            userController.register(registerForm, bindingResult)
        }

        assert(bindingResult.hasErrors())
    }

    @Test
    fun `correct login`() {
        whenever(userService.findByCredentials(LOGIN, EMAIL, PASS)).thenReturn(Optional.of(user))
        whenever(jwtService.create(user)).thenReturn("jwt")

        val enterForm = EnterForm(LOGIN, EMAIL, PASS)

        val bindingResult: BindingResult = DirectFieldBindingResult(this, "test")

        val response = Assertions.assertDoesNotThrow(ThrowingSupplier {
            userController.login(enterForm, bindingResult)
        })

        assert(!bindingResult.hasErrors())

        assert(response.statusCode == HttpStatus.OK)
        Assertions.assertNotNull(response.body)

        val jwt = response.body!!
        assert(jwt.isNotBlank())
    }

    @Test
    fun `invalid enter form`() {
        val enterForm = EnterForm(INVALID, EMAIL, PASS)

        val bindingResult: BindingResult = DirectFieldBindingResult(this, "test")
        bindingResult.reject("not-found", "test")

        assertThrows<ValidationException> {
            userController.login(enterForm, bindingResult)
        }

        assert(bindingResult.hasErrors())
    }

    private companion object {
        private const val USERNAME = "test"
        private const val LOGIN = "login"
        private const val EMAIL = "email@test.com"
        private const val PASS = "pass"

        private val user = User(USERNAME, LOGIN, EMAIL, PASS, Role.ANONYMOUS)

        private const val INVALID = "invalid"
    }
}
