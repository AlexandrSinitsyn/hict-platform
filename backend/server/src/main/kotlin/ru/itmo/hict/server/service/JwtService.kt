package ru.itmo.hict.server.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Service
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.dto.USER_ID_CLAIM
import ru.itmo.hict.entity.User
import java.util.*

@Service
class JwtService(
    private val userService: UserService,
    algorithm: Algorithm,
) {
    private val verifier: JWTVerifier = JWT.require(algorithm).build()

    fun find(jwt: Jwt): User? {
        val decodedJwt: DecodedJWT = verifier.verify(jwt)
        return decodedJwt.getClaim(USER_ID_CLAIM)?.asString()?.let { userService.getById(UUID.fromString(it)) }
    }
}
