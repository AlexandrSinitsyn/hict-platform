package ru.itmo.hict.server

import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.itmo.hict.entity.Role
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.config.RequestUserInfo
import ru.itmo.hict.server.controller.UserController
import ru.itmo.hict.server.exception.ValidationException
import ru.itmo.hict.server.repository.UserRepository
import ru.itmo.hict.server.service.UserService
import java.sql.Timestamp
import java.util.*
import kotlin.random.Random

@WebMvcTest(UserController::class)
@ContextConfiguration(
    classes = [UserController::class, UserService::class, UserRestTests.RestTestBeans::class]
)
class UserRestTests {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var userRepository: UserRepository

    @Test
    fun contextLoads() {
    }

    private fun expectBadRequest(url: String, vararg body: String) {
        body.forEach {
            assertDoesNotThrow {
                mvc.perform(
                    patch("/api/v1/users/$url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
            }
        }
    }

    private fun expectValidationException(url: String, vararg test: Pair<String, List<String>>) {
        test.forEach { (body, exceptions) ->
            mvc.perform(
                patch("/api/v1/users/$url")
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

    @Test
    fun `users count`() {
        val count = Random.nextLong()
        whenever(userRepository.count()) doReturn count

        mvc.perform(get("/api/v1/users/count"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("$count"))
    }

    @Nested
    inner class UpdateUserInfoForm {
        private val NEW_USERNAME = "newUsername"
        private val NEW_LOGIN = "newLogin"
        private val NEW_EMAIL = "new@email.com"

        private fun jsonBody(username: String?, login: String?, email: String?) = """
            {
                "username": ${username?.let { "\"$it\"" }},
                "login": ${login?.let { "\"$it\"" }},
                "email": ${email?.let { "\"$it\"" }}
            }
        """.trimIndent()

        @Test
        fun `correct update`() {
            whenever(userRepository.findById(USER_ID)) doReturn Optional.of(user)
            doNothing().whenever(userRepository).updateUsername(any(), any())
            doNothing().whenever(userRepository).updateLogin(any(), any())
            doNothing().whenever(userRepository).updateEmail(any(), any())

            mvc.perform(
                patch("/api/v1/users/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody(NEW_USERNAME, NEW_LOGIN, NEW_EMAIL)))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"))
        }

        @Test
        fun `correct partial update`() {
            whenever(userRepository.findById(USER_ID)) doReturn Optional.of(user)
            doNothing().whenever(userRepository).updateUsername(any(), any())
            doNothing().whenever(userRepository).updateLogin(any(), any())
            doNothing().whenever(userRepository).updateEmail(any(), any())

            fun run(username: String?, login: String?, email: String?) {
                mvc.perform(
                    patch("/api/v1/users/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody(username, login, email)))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string("true"))
            }

            run(NEW_USERNAME, null, null)
            run(null, NEW_LOGIN, null)
            run(null, null, NEW_EMAIL)
        }

        @Test
        fun `invalid update`() {
            mvc.perform(
                patch("/api/v1/users/update/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody(user.username, user.login, user.email)))
                .andExpect {
                    Assertions.assertNotNull(it.resolvedException)
                    val err = it.resolvedException.run {
                        assert(this is ValidationException)

                        this as ValidationException
                    }.bindingResult

                    assert(err.hasErrors())
                    assert(err.allErrors.first().defaultMessage!!.lowercase().contains("should be different"))
                }
        }

        @Test
        fun `no data`() {
            expectBadRequest("update/info", "", "{}")
        }

        @Test
        fun `invalid form`() {
            expectBadRequest("update/info",
                """
                    {
                        "invalid": "unsupported"
                    }
                """.trimIndent()
            )
        }

        @Test
        fun `null field in the form`() {
            expectBadRequest("update/info",
                jsonBody(null, null, null),
            )
        }

        @Test
        fun `empty field form`() {
            expectValidationException("update/info",
                jsonBody("", NEW_LOGIN, NEW_EMAIL) throws IS_BLANK or INVALID_SIZE,
                jsonBody(NEW_USERNAME, "", NEW_EMAIL) throws IS_BLANK or INVALID_SIZE,
                jsonBody(NEW_USERNAME, NEW_LOGIN, "") throws IS_BLANK or INVALID_SIZE or NOT_EMAIL_TYPE,
            )
        }

        @Test
        fun `blank field form`() {
            expectValidationException("update/info",
                jsonBody("    ", NEW_LOGIN, NEW_EMAIL) throws IS_BLANK,
                jsonBody(NEW_USERNAME, "    ", NEW_EMAIL) throws IS_BLANK,
                jsonBody(NEW_USERNAME, NEW_LOGIN, "    ") throws IS_BLANK or NOT_EMAIL_TYPE,
            )
        }

        @Test
        fun `invalid length field form`() {
            expectValidationException("update/info",
                jsonBody("x", NEW_LOGIN, NEW_EMAIL) throws INVALID_SIZE,
                jsonBody(NEW_USERNAME, "x", NEW_EMAIL) throws INVALID_SIZE,
                jsonBody(NEW_USERNAME, NEW_LOGIN, "x") throws INVALID_SIZE or NOT_EMAIL_TYPE,
            )
        }

        @Test
        fun `invalid email type form`() {
            expectValidationException("update/info",
                jsonBody(NEW_USERNAME, NEW_LOGIN, "invalidEmailType") throws NOT_EMAIL_TYPE,
            )
        }
    }

    @Nested
    inner class UpdatePassword {
        private val PASS = "pass"
        private val NEW_PASS = "newPass"

        private fun jsonBody(oldPassword: String?, newPassword: String?) = """
            {
                "oldPassword": ${oldPassword?.let { "\"$it\"" }},
                "newPassword": ${newPassword?.let { "\"$it\"" }}
            }
        """.trimIndent()

        @Test
        fun `correct update`() {
            whenever(userRepository.findById(USER_ID)) doReturn Optional.of(user)
            whenever(userRepository.findByLoginAndPassword(any(), any())) doReturn Optional.of(user)
            doNothing().whenever(userRepository).updatePassword(any(), any(), any())

            mvc.perform(
                patch("/api/v1/users/update/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody(PASS, NEW_PASS)))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"))
        }

        @Test
        fun `invalid update`() {
            whenever(userRepository.findByLoginAndPassword(any(), any())) doReturn Optional.of(user)

            mvc.perform(
                patch("/api/v1/users/update/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody(PASS, PASS)))
                .andExpect {
                    Assertions.assertNotNull(it.resolvedException)
                    val err = it.resolvedException.run {
                        assert(this is ValidationException)

                        this as ValidationException
                    }.bindingResult

                    assert(err.hasErrors())
                    assert(err.allErrors.first().defaultMessage!!.lowercase().contains("should be different"))
                }
        }

        @Test
        fun `invalid credentials`() {
            whenever(userRepository.findByLoginAndPassword(any(), any())) doReturn Optional.empty()

            mvc.perform(
                patch("/api/v1/users/update/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody(PASS, PASS)))
                .andExpect {
                    Assertions.assertNotNull(it.resolvedException)
                    val err = it.resolvedException.run {
                        assert(this is ValidationException)

                        this as ValidationException
                    }.bindingResult

                    assert(err.hasErrors())
                    assert(err.allErrors.first().defaultMessage!!.lowercase().contains("must confirm"))
                }
        }

        @Test
        fun `no data`() {
            expectBadRequest("update/password", "", "{}")
        }

        @Test
        fun `invalid form`() {
            expectBadRequest("update/password",
                """
                    {
                        "invalid": "unsupported"
                    }
                """.trimIndent()
            )
        }

        @Test
        fun `null field in the form`() {
            expectBadRequest("update/password",
                jsonBody(null, NEW_PASS),
                jsonBody(PASS, null),
            )
        }

        @Test
        fun `empty field form`() {
            expectValidationException("update/password",
                jsonBody("", NEW_PASS) throws IS_BLANK or INVALID_SIZE,
                jsonBody(PASS, "") throws IS_BLANK or INVALID_SIZE,
            )
        }

        @Test
        fun `blank field form`() {
            expectValidationException("update/password",
                jsonBody("    ", NEW_PASS) throws IS_BLANK,
                jsonBody(PASS, "    ") throws IS_BLANK,
            )
        }

        @Test
        fun `invalid length field form`() {
            expectValidationException("update/password",
                jsonBody("x", NEW_PASS) throws INVALID_SIZE,
                jsonBody(PASS, "x") throws INVALID_SIZE,
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
        private const val USER_ID = 1L
        private val user = User(
            "username", "login", "email@test.com", "pass", Role.USER,
            id = USER_ID, creationTime = Timestamp(System.currentTimeMillis())
        )

        private const val IS_BLANK = "blank"
        private const val INVALID_SIZE = "size must be between"
        private const val NOT_EMAIL_TYPE = "must be a well-formed email"
    }
}
