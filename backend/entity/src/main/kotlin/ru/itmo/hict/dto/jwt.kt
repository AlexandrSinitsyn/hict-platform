package ru.itmo.hict.dto

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

typealias Jwt = String

@Service
class JwtService(
    @Value("\${JWT_SECRET}") private val secret: String,
) {
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun create(userInfoDto: UserInfoDto): Jwt {
        return JWT.create()
            .withClaim("userId", userInfoDto.id)
            .sign(algorithm)
    }
}
