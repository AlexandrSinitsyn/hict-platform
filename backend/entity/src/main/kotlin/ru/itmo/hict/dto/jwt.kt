package ru.itmo.hict.dto

import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

typealias Jwt = String

const val USER_ID_CLAIM = "userId"

@Configuration
class JwtConfig(
    @Value("\${JWT_SECRET}") private val secret: String,
) {
    @Bean
    fun algorithm(): Algorithm = Algorithm.HMAC256(secret)
}
