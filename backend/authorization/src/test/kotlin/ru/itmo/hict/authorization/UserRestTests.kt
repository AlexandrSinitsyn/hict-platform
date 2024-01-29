package ru.itmo.hict.authorization

import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import ru.itmo.hict.authorization.controller.UserController
import ru.itmo.hict.authorization.exceptions.ValidationException
import ru.itmo.hict.authorization.repositories.UserRepository
import ru.itmo.hict.authorization.service.JwtService
import ru.itmo.hict.authorization.service.UserService
import ru.itmo.hict.authorization.validators.EnterFormValidator
import ru.itmo.hict.authorization.validators.RegisterFormValidator
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import java.util.*

@WebMvcTest(UserController::class)
@Import(UserRestTests.TestExceptionHandler::class)
@ContextConfiguration(
    classes = [UserController::class, UserService::class, RegisterFormValidator::class, EnterFormValidator::class]
)
class UserRestTests {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var jwtService: JwtService
    @MockBean
    private lateinit var userRepository: UserRepository

    @PostConstruct
    fun setup() {
        whenever(jwtService.create(user)).thenReturn("jwt")
    }

    @Test
    fun contextLoads() {
    }

    private fun expectBadRequest(url: String, body: String) {
        assertDoesNotThrow {
            mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest)
        }
    }

    private fun expectValidationException(url: String, body: String) {
        assertDoesNotThrow {
            mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect {
                    Assertions.assertNotNull(it.resolvedException)
                    Assertions.assertInstanceOf(ValidationException::class.java, it.resolvedException)
                    assert((it.resolvedException!! as ValidationException).bindingResult.hasErrors())
                }
        }
    }

    @Nested
    inner class Registration {
        private fun jsonBody(username: String?, login: String?, email: String?, password: String?) = """
        {
            "username": ${username?.let { "\"$it\"" }},
            "login": ${login?.let { "\"$it\"" }},
            "email": ${email?.let { "\"$it\"" }},
            "passwordSha": ${password?.let { "\"$it\"" }}
        }
    """.trimIndent()

        @Test
        fun `valid registration`() {
            whenever(userRepository.findByLoginOrEmail(LOGIN, EMAIL)).thenReturn(Optional.empty())
            whenever(userRepository.save(any())).thenReturn(Optional.of(user))

            mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody(USERNAME, LOGIN, EMAIL, PASS)))
                .andExpect(status().isOk)
                .andExpect(content().string("jwt"))
        }

        @Test
        fun `invalid registration`() {
            whenever(userRepository.findByLoginOrEmail(any(), any())).thenReturn(Optional.of(user))
            whenever(userRepository.save(any())).thenReturn(Optional.empty())

            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, EMAIL, PASS))
        }

        @Test
        fun `invalid content type registration form`() {
            mvc.perform(post("/api/v1/auth/register"))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `empty registration form`() {
            expectBadRequest("/api/v1/auth/register", "{}")
        }

        @Test
        fun `invalid registration form`() {
            expectBadRequest("/api/v1/auth/register", """
            {
                "invalid": "unsupported"
            }
        """.trimIndent())
        }

        @Test
        fun `null field registration form`() {
            expectBadRequest("/api/v1/auth/register", jsonBody(null, LOGIN, EMAIL, PASS))
            expectBadRequest("/api/v1/auth/register", jsonBody(USERNAME, null, EMAIL, PASS))
            expectBadRequest("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, null, PASS))
            expectBadRequest("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, EMAIL, null))
        }

        @Test
        fun `empty field registration form`() {
            expectValidationException("/api/v1/auth/register", jsonBody("", LOGIN, EMAIL, PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, "", EMAIL, PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, "", PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, EMAIL, ""))
        }

        @Test
        fun `blank field registration form`() {
            expectValidationException("/api/v1/auth/register", jsonBody("    ", LOGIN, EMAIL, PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, "    ", EMAIL, PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, "    ", PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, EMAIL, "    "))
        }

        @Test
        fun `invalid length field registration form`() {
            expectValidationException("/api/v1/auth/register", jsonBody("x", LOGIN, EMAIL, PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, "x", EMAIL, PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, "x", PASS))
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, EMAIL, "x"))
        }

        @Test
        fun `invalid email field registration form`() {
            expectValidationException("/api/v1/auth/register", jsonBody(USERNAME, LOGIN, "invalid", PASS))
        }
    }

    @Nested
    inner class Login {
        private fun jsonBody(login: String?, email: String?, password: String?) = """
        {
            "login": ${login?.let { "\"$it\"" }},
            "email": ${email?.let { "\"$it\"" }},
            "passwordSha": ${password?.let { "\"$it\"" }}
        }
    """.trimIndent()

        @Test
        fun `valid registration`() {
            whenever(userRepository.findByLoginOrEmailAndPasswordSha(LOGIN, EMAIL, PASS)).thenReturn(Optional.of(user))
            whenever(userRepository.findByLoginOrEmailAndPasswordSha(null, EMAIL, PASS)).thenReturn(Optional.of(user))
            whenever(userRepository.findByLoginOrEmailAndPasswordSha(LOGIN, null, PASS)).thenReturn(Optional.of(user))

            fun expectOk(login: String?, email: String?, password: String) =
                mvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody(login, email, password)))
                    .andExpect(status().isOk)
                    .andExpect(content().string("jwt"))

            expectOk(LOGIN, EMAIL, PASS)
            expectOk(null, EMAIL, PASS)
            expectOk(LOGIN, null, PASS)
        }

        @Test
        fun `invalid login`() {
            whenever(userRepository.findByLoginOrEmailAndPasswordSha(any(), any(), any())).thenReturn(Optional.empty())

            expectValidationException("/api/v1/auth/login", jsonBody(LOGIN, EMAIL, PASS))
        }

        @Test
        fun `invalid content type registration form`() {
            mvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `empty registration form`() {
            expectBadRequest("/api/v1/auth/login", "{}")
        }

        @Test
        fun `invalid registration form`() {
            expectBadRequest("/api/v1/auth/login", """
            {
                "invalid": "unsupported"
            }
        """.trimIndent())
        }

        @Test
        fun `null field registration form`() {
            expectValidationException("/api/v1/auth/login", jsonBody(null, null, PASS))
            expectBadRequest("/api/v1/auth/login", jsonBody(LOGIN, EMAIL, null))
        }

        @Test
        fun `empty field registration form`() {
            expectValidationException("/api/v1/auth/login", jsonBody("", EMAIL, PASS))
            expectValidationException("/api/v1/auth/login", jsonBody(LOGIN, "", PASS))
            expectValidationException("/api/v1/auth/login", jsonBody(LOGIN, EMAIL, ""))
        }

        @Test
        fun `blank field registration form`() {
            expectValidationException("/api/v1/auth/login", jsonBody("    ", EMAIL, PASS))
            expectValidationException("/api/v1/auth/login", jsonBody(LOGIN, "    ", PASS))
            expectValidationException("/api/v1/auth/login", jsonBody(LOGIN, EMAIL, "    "))
        }

        @Test
        fun `invalid length field registration form`() {
            expectValidationException("/api/v1/auth/login", jsonBody("x", EMAIL, PASS))
            expectValidationException("/api/v1/auth/login", jsonBody(LOGIN, "x", PASS))
            expectValidationException("/api/v1/auth/login", jsonBody(LOGIN, EMAIL, "x"))
        }

        @Test
        fun `invalid email field registration form`() {
            expectValidationException("/api/v1/auth/login", jsonBody(LOGIN, "invalid", PASS))
        }
    }

    @TestComponent
    @ControllerAdvice
    class TestExceptionHandler {
        @ExceptionHandler(ValidationException::class)
        fun exceptionHandler(e: ValidationException) {
            assert(e.bindingResult.hasErrors())
        }
    }

    private companion object {
        private const val USERNAME = "test"
        private const val LOGIN = "login"
        private const val EMAIL = "email@test.com"
        private const val PASS = "pass"

        private val user = User(USERNAME, LOGIN, EMAIL, PASS, Role.ANONYMOUS)
    }
}
