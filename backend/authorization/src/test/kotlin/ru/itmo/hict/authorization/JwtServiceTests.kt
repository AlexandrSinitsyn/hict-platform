package ru.itmo.hict.authorization

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.itmo.hict.authorization.service.JwtService
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.dto.USER_ID_CLAIM
import ru.itmo.hict.entity.User
import java.util.*
import kotlin.collections.HashSet
import kotlin.random.Random

class JwtServiceTests {
    private class JwtDecoder(algorithm: Algorithm) {
        private val verifier: JWTVerifier = JWT.require(algorithm).build()

        fun find(jwt: Jwt): UUID? {
            val decodedJwt: DecodedJWT = verifier.verify(jwt)
            return decodedJwt.getClaim(USER_ID_CLAIM)?.asString()?.run { UUID.fromString(this) }
        }
    }

    private lateinit var encoder: JwtService
    private lateinit var decoder: JwtDecoder
    private val user = User(
        "test", "test", "test@test.com", "test",
        id = UUID.randomUUID()
    )

    @BeforeEach
    fun setup() {
        val algorithm = Algorithm.HMAC256("${System.currentTimeMillis()}")

        encoder = JwtService(algorithm)
        decoder = JwtDecoder(algorithm)
    }

    @Test
    fun initialized() {
        Assertions.assertNotNull(encoder)
        Assertions.assertNotNull(decoder)
    }

    @Test
    fun `encoded value not null`() {
        val jwt = encoder.create(user)

        Assertions.assertNotNull(jwt)
        Assertions.assertTrue(jwt.isNotBlank())
    }

    @Test
    fun `encode - decoded`() {
        val decoded = decoder.find(encoder.create(user))

        Assertions.assertNotNull(decoded)
        Assertions.assertInstanceOf(UUID::class.javaObjectType, decoded)
        Assertions.assertEquals(user.id, decoded)
    }

    @Test
    fun `encoding different`() {
        val base: Jwt = encoder.create(user)

        val rndString: () -> String = {
            (1..Random.nextInt(100)).joinToString("") { ('a'..'z').random().toString() }
        }

        val jwts = HashSet<String>()
        val ids = HashSet<UUID>()

        repeat(10_000) {
            val test = User(rndString(), rndString(), rndString(), rndString(), id = UUID.randomUUID())

            val encoded = encoder.create(test)

            Assertions.assertNotNull(encoded)
            Assertions.assertNotEquals(base, encoded)

            val decoded = decoder.find(encoded)
            Assertions.assertNotNull(decoded)
            Assertions.assertNotEquals(user.id, decoded)
            Assertions.assertEquals(decoded, test.id)

            Assertions.assertFalse(encoded in jwts)
            Assertions.assertFalse(decoded in ids)

            jwts += encoded
            ids += decoded!!
        }
    }
}
