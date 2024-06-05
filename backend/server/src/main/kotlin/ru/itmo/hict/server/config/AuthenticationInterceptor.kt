package ru.itmo.hict.server.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.server.exception.InvalidAuthorizationTypeException
import ru.itmo.hict.server.exception.InvalidJwtException
import ru.itmo.hict.server.service.JwtService

@Component
class AuthenticationInterceptor(
    private val jwtService: JwtService,
) : HandlerInterceptor {
    @Autowired
    private lateinit var requestUserInfo: RequestUserInfo

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        requestUserInfo.jwt = null
        requestUserInfo.user = null

        val auth = request.getHeader("Authorization") ?: return true

        if (!auth.startsWith("Bearer")) {
            throw InvalidAuthorizationTypeException(auth)
        }

        val jwt: Jwt = auth.substring(7)
        val user = jwtService.find(jwt) ?: throw InvalidJwtException()

        requestUserInfo.jwt = jwt
        requestUserInfo.user = user

        return true
    }
}
