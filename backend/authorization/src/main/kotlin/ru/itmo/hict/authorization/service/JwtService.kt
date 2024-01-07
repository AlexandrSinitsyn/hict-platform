package ru.itmo.hict.authorization.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.stereotype.Service
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.dto.USER_ID_CLAIM
import ru.itmo.hict.entity.User

@Service
class JwtService(
    private val algorithm: Algorithm,
) {
    fun create(user: User): Jwt {
        return JWT.create()
            .withClaim(USER_ID_CLAIM, user.id)
            .sign(algorithm)
    }
}
