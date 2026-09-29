package com.jilinjobs.cms.security

import com.jilinjobs.cms.identity.CmsIdentityConverter
import com.jilinjobs.cms.identity.CmsRole
import com.jilinjobs.cms.identity.CredentialVerifier
import com.jilinjobs.cms.identity.VerifiedIdentity
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration(proxyBeanMethods = false)
class ReviewIdentityConfiguration {
    @Bean
    fun reviewIdentityAuthenticationConfigurer(
        @Value("\${cms.review-identity.token}") token: String,
    ): CmsHttpAuthenticationConfigurer {
        require(token.isNotBlank()) { "Review identity token must be explicitly configured" }
        val verifier = ReviewCredentialVerifier(token)
        val converter = CmsIdentityConverter(
            listOf(verifier),
            mapOf(verifier.identitySource to mapOf(ReviewCredentialVerifier.ADMIN_ROLE to CmsRole.ADMIN)),
        )
        val filter = ReviewIdentityFilter(converter, verifier.identitySource)
        return CmsHttpAuthenticationConfigurer { http ->
            http.csrf { csrf -> csrf.ignoringRequestMatchers("/api/admin/**") }
            http.sessionManagement { sessions ->
                sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            http.addFilterBefore(filter, AnonymousAuthenticationFilter::class.java)
        }
    }
}

private class ReviewCredentialVerifier(
    private val expectedToken: String,
) : CredentialVerifier {
    override val identitySource: String = "local-review"

    override fun verify(credential: String): VerifiedIdentity? =
        if (credential == expectedToken) VerifiedIdentity(REVIEW_USER_ID, setOf(ADMIN_ROLE)) else null

    companion object {
        const val ADMIN_ROLE = "review-admin"
        const val REVIEW_USER_ID = "local-review-admin"
    }
}

private class ReviewIdentityFilter(
    private val converter: CmsIdentityConverter,
    private val identitySource: String,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith("/api/admin/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val credential = request.getHeader(REVIEW_CREDENTIAL_HEADER)
            ?.takeIf { it.isNotBlank() }

        if (credential != null && SecurityContextHolder.getContext().authentication == null) {
            runCatching { converter.authenticate(identitySource, credential) }
                .getOrNull()
                ?.let { principal -> SecurityContextHolder.getContext().authentication = principal.toSpringAuthentication() }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        private const val REVIEW_CREDENTIAL_HEADER = "X-Cms-Review-Credential"
    }
}
