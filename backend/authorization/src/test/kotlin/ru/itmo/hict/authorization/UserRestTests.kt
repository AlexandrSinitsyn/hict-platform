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
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
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

    private fun expectBadRequest(url: String, vararg body: String) {
        body.forEach {
            assertDoesNotThrow {
                mvc.perform(post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(it))
                    .andExpect(status().isBadRequest)
            }
        }
    }

    private fun expectValidationException(url: String, vararg test: Pair<String, List<String>>) {
        test.forEach { (body, exceptions) ->
            mvc.perform(
                post(url)
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

    @Nested
    inner class Registration {
        private fun jsonBody(username: String?, login: String?, email: String?, password: String?) = """
            {
                "username": ${username?.let { "\"$it\"" }},
                "login": ${login?.let { "\"$it\"" }},
                "email": ${email?.let { "\"$it\"" }},
                "password": ${password?.let { "\"$it\"" }}
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

            expectValidationException("/api/v1/auth/register",
                jsonBody(USERNAME, LOGIN, EMAIL, PASS) throws "occupied"
            )
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
            expectBadRequest("/api/v1/auth/register",
                jsonBody(null, LOGIN, EMAIL, PASS),
                jsonBody(USERNAME, null, EMAIL, PASS),
                jsonBody(USERNAME, LOGIN, null, PASS),
                jsonBody(USERNAME, LOGIN, EMAIL, null)
            )
        }

        @Test
        fun `empty field registration form`() {
            expectValidationException("/api/v1/auth/register",
                jsonBody("", LOGIN, EMAIL, PASS) throws IS_BLANK or INVALID_SIZE,
                jsonBody(USERNAME, "", EMAIL, PASS) throws IS_BLANK or INVALID_SIZE,
                jsonBody(USERNAME, LOGIN, "", PASS) throws IS_BLANK or INVALID_SIZE or NOT_EMAIL_TYPE,
                jsonBody(USERNAME, LOGIN, EMAIL, "") throws IS_BLANK or INVALID_SIZE
            )
        }

        @Test
        fun `blank field registration form`() {
            expectValidationException("/api/v1/auth/register",
                jsonBody("    ", LOGIN, EMAIL, PASS) throws IS_BLANK,
                jsonBody(USERNAME, "    ", EMAIL, PASS) throws IS_BLANK,
                jsonBody(USERNAME, LOGIN, "    ", PASS) throws IS_BLANK or NOT_EMAIL_TYPE,
                jsonBody(USERNAME, LOGIN, EMAIL, "    ") throws IS_BLANK
            )
        }

        @Test
        fun `invalid length field registration form`() {
            expectValidationException("/api/v1/auth/register",
                jsonBody("x", LOGIN, EMAIL, PASS) throws INVALID_SIZE,
                jsonBody(USERNAME, "x", EMAIL, PASS) throws INVALID_SIZE,
                jsonBody(USERNAME, LOGIN, "x", PASS) throws NOT_EMAIL_TYPE,
                jsonBody(USERNAME, LOGIN, EMAIL, "x") throws INVALID_SIZE
            )
        }

        @Test
        fun `invalid email field registration form`() {
            expectValidationException("/api/v1/auth/register",
                jsonBody(USERNAME, LOGIN, "invalid", PASS) throws NOT_EMAIL_TYPE)
        }
    }

    @Nested
    inner class Login {
        private fun jsonBody(login: String?, email: String?, password: String?) = """
            {
                "login": ${login?.let { "\"$it\"" }},
                "email": ${email?.let { "\"$it\"" }},
                "password": ${password?.let { "\"$it\"" }}
            }
        """.trimIndent()

        @Test
        fun `valid registration`() {
            whenever(userRepository.findByLoginOrEmailAndPassword(LOGIN, EMAIL, PASS)).thenReturn(Optional.of(user))
            whenever(userRepository.findByLoginOrEmailAndPassword(null, EMAIL, PASS)).thenReturn(Optional.of(user))
            whenever(userRepository.findByLoginOrEmailAndPassword(LOGIN, null, PASS)).thenReturn(Optional.of(user))

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
            whenever(userRepository.findByLoginOrEmailAndPassword(any(), any(), any())).thenReturn(Optional.empty())

            expectValidationException("/api/v1/auth/login",
                jsonBody(LOGIN, EMAIL, PASS) throws "Invalid login or password")
        }

        @Test
        fun `invalid content type login form`() {
            mvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `empty login form`() {
            expectBadRequest("/api/v1/auth/login", "{}")
        }

        @Test
        fun `invalid login form`() {
            expectBadRequest("/api/v1/auth/login", """
            {
                "invalid": "unsupported"
            }
        """.trimIndent())
        }

        @Test
        fun `null field login form`() {
            expectValidationException("/api/v1/auth/login",
                jsonBody(null, null, PASS) throws "require at least one")
            expectBadRequest("/api/v1/auth/login",
                jsonBody(LOGIN, EMAIL, null))
        }

        @Test
        fun `empty field login form`() {
            expectValidationException("/api/v1/auth/login",
                jsonBody("", EMAIL, PASS) throws IS_BLANK or INVALID_SIZE,
                jsonBody(LOGIN, "", PASS) throws IS_BLANK or INVALID_SIZE or NOT_EMAIL_TYPE,
                jsonBody(LOGIN, EMAIL, "") throws IS_BLANK or INVALID_SIZE
            )
        }

        @Test
        fun `blank field login form`() {
            expectValidationException("/api/v1/auth/login",
                jsonBody("    ", EMAIL, PASS) throws IS_BLANK,
                jsonBody(LOGIN, "    ", PASS) throws IS_BLANK or NOT_EMAIL_TYPE,
                jsonBody(LOGIN, EMAIL, "    ") throws IS_BLANK
            )
        }

        @Test
        fun `invalid length field login form`() {
            expectValidationException("/api/v1/auth/login",
                jsonBody("x", EMAIL, PASS) throws INVALID_SIZE,
                jsonBody(LOGIN, "x", PASS) throws NOT_EMAIL_TYPE,
                jsonBody(LOGIN, EMAIL, "x") throws INVALID_SIZE
            )
        }

        @Test
        fun `invalid email field login form`() {
            expectValidationException("/api/v1/auth/login",
                jsonBody(LOGIN, "invalid", PASS) throws NOT_EMAIL_TYPE)
        }
    }

    private companion object {
        private const val USERNAME = "test"
        private const val LOGIN = "login"
        private const val EMAIL = "email@test.com"
        private const val PASS = "pass"

        private val user = User(USERNAME, LOGIN, EMAIL, PASS, Role.ANONYMOUS)
        
        private const val IS_BLANK = "blank"
        private const val INVALID_SIZE = "size must be between"
        private const val NOT_EMAIL_TYPE = "must be a well-formed email"
    }
}
