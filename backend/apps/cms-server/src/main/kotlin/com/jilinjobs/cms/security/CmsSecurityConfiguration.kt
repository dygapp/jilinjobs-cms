package com.jilinjobs.cms.security

import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.csrf.CsrfException
import org.springframework.security.web.SecurityFilterChain

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
class CmsSecurityConfiguration(
    private val authenticationConfigurers: ObjectProvider<CmsHttpAuthenticationConfigurer>,
) {
    @Bean
    fun cmsSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorize ->
            authorize
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/**", "/static/**").permitAll()
                .requestMatchers("/api/admin/**").authenticated()
                .anyRequest().denyAll()
        }
        http.csrf { }
        http.formLogin { form -> form.disable() }
        http.httpBasic { basic -> basic.disable() }
        http.logout { logout -> logout.disable() }
        http.requestCache { cache -> cache.disable() }
        authenticationConfigurers.orderedStream().forEach { configurer -> configurer.configure(http) }
        http.exceptionHandling { exceptions ->
            exceptions.authenticationEntryPoint { _, response, _ ->
                writeSecurityFailure(response, HttpStatus.UNAUTHORIZED, "管理身份未认证")
            }
            exceptions.accessDeniedHandler { _, response, denied ->
                val authentication = SecurityContextHolder.getContext().authentication
                if (denied is CsrfException && authentication?.isAuthenticated != true) {
                    writeSecurityFailure(response, HttpStatus.UNAUTHORIZED, "管理身份未认证")
                } else {
                    writeSecurityFailure(response, HttpStatus.FORBIDDEN, "管理身份无访问权限")
                }
            }
        }
        return http.build()
    }
}

private fun writeSecurityFailure(response: HttpServletResponse, status: HttpStatus, message: String) {
    response.status = status.value()
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    response.characterEncoding = StandardCharsets.UTF_8.name()
    response.writer.write("""{"message":"$message"}""")
}
