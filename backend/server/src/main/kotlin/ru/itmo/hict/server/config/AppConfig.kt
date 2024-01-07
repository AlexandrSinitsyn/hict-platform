package ru.itmo.hict.server.config

import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ru.itmo.hict.dto.Jwt
import ru.itmo.hict.entity.User

@Component
@RequestScope
data class RequestUserInfo @JvmOverloads constructor(
    var jwt: Jwt? = null,
    var user: User? = null,
)

@Configuration
class AppConfig(
    private val authenticationInterceptor: AuthenticationInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }
}
