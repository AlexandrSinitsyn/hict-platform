package ru.itmo.hict.authorization.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.entity.User

@Service
class JwtService(
    @Value("\${JWT_SECRET}") private val secret: String,
) {
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun create(user: User): Jwt {
        return JWT.create()
            .withClaim("userId", user.id)
            .sign(algorithm)
    }
}
