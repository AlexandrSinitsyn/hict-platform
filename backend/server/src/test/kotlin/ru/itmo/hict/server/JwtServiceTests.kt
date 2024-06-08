package ru.itmo.hict.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.dto.USER_ID_CLAIM
import ru.itmo.hict.entity.User
import ru.itmo.hict.server.service.JwtService
import ru.itmo.hict.server.service.UserService
import java.util.*
import kotlin.collections.HashSet
import kotlin.random.Random

class JwtServiceTests {
    private class JwtEncoder(private val algorithm: Algorithm) {
        fun create(user: User): Jwt {
            return JWT.create()
                .withClaim(USER_ID_CLAIM, "${user.id}")
                .sign(algorithm)
        }
    }

    private lateinit var encoder: JwtEncoder
    private lateinit var decoder: JwtService

    private companion object {
        private lateinit var userService: UserService
        private val user = User(
            "test", "test", "test@test.com", "test",
            id = UUID.randomUUID()
        )

        @JvmStatic
        @BeforeAll
        fun init() {
            userService = mock<UserService>()
            whenever(userService.getById(user.id!!)).thenReturn(user)
        }
    }

    @BeforeEach
    fun setup() {
        val algorithm = Algorithm.HMAC256("${System.currentTimeMillis()}")

        encoder = JwtEncoder(algorithm)
        decoder = JwtService(userService, algorithm)
    }

    @Test
    fun initialized() {
        Assertions.assertNotNull(encoder)
        Assertions.assertNotNull(decoder)
    }

    @Test
    fun `encode - decoded`() {
        val decoded = decoder.find(encoder.create(user))

        Assertions.assertNotNull(decoded)
        Assertions.assertNotNull(decoded!!.id)
        Assertions.assertEquals(user.id, decoded.id)
    }

    @Test
    fun `encoding invalid`() {
        Assertions.assertThrows(JWTVerificationException::class.java) { decoder.find("invalid") }
    }

    @Test
    fun `decoding correct`() {
        val rndString: () -> String = {
            (1..Random.nextInt(100)).joinToString("") { ('a'..'z').random().toString() }
        }

        val jwts = HashSet<String>()

        repeat(10_000) {
            val test = User(rndString(), rndString(), rndString(), rndString(), id = UUID.randomUUID())

            whenever(userService.getById(test.id!!)).thenReturn(test)

            val encoded = encoder.create(test)

            val decoded = decoder.find(encoded)

            Assertions.assertNotNull(decoded)
            Assertions.assertNotNull(decoded!!.id)
            Assertions.assertEquals(test.id, decoded.id)

            Assertions.assertFalse(encoded in jwts)

            jwts += encoded
        }
    }
}
