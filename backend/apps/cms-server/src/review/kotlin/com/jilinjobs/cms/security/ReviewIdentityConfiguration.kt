package com.jilinjobs.cms.security

import com.jilinjobs.cms.identity.AdminIdentityResponse
import com.jilinjobs.cms.identity.CmsIdentityConverter
import com.jilinjobs.cms.identity.CmsRole
import com.jilinjobs.cms.identity.CredentialVerifier
import com.jilinjobs.cms.identity.VerifiedIdentity
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.filter.OncePerRequestFilter

const val REVIEW_CREDENTIAL_HEADER = "X-Cms-Review-Credential"
private const val REVIEW_RESOURCE_COOKIE = "cms_review_resource"

@Configuration(proxyBeanMethods = false)
class ReviewIdentityConfiguration {
    @Bean
    fun reviewIdentitySessions(
        @Value("\${cms.review-identity.admin-token}") adminToken: String,
        @Value("\${cms.review-identity.super-token}") superToken: String,
        @Value("\${cms.review-identity.session-ttl:PT30M}") sessionTtl: Duration,
        @Value("\${cms.review-identity.max-sessions:256}") maxSessions: Int,
    ): ReviewIdentitySessions = ReviewIdentitySessions(adminToken, superToken, sessionTtl, maxSessions)

    @Bean
    fun reviewIdentityAuthenticationConfigurer(sessions: ReviewIdentitySessions): CmsHttpAuthenticationConfigurer {
        val verifier = ReviewCredentialVerifier(sessions)
        val converter = CmsIdentityConverter(
            listOf(verifier),
            mapOf(
                verifier.identitySource to mapOf(
                    ReviewIdentityProfile.ADMIN.externalRole to CmsRole.ADMIN,
                    ReviewIdentityProfile.SUPER.externalRole to CmsRole.SUPER,
                ),
            ),
        )
        val filter = ReviewIdentityFilter(converter, verifier.identitySource)
        return CmsHttpAuthenticationConfigurer { http ->
            http.csrf { csrf -> csrf.ignoringRequestMatchers("/api/admin/**") }
            http.sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            http.addFilterBefore(filter, AnonymousAuthenticationFilter::class.java)
        }
    }

    @Bean
    @Order(1)
    fun reviewIdentityEndpointSecurity(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher("/api/review/**")
        http.authorizeHttpRequests { authorize -> authorize.anyRequest().permitAll() }
        http.csrf { csrf -> csrf.disable() }
        http.sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        http.formLogin { form -> form.disable() }
        http.httpBasic { basic -> basic.disable() }
        http.logout { logout -> logout.disable() }
        http.requestCache { cache -> cache.disable() }
        return http.build()
    }
}

enum class ReviewIdentityProfile(
    val token: String,
    val userId: String,
    val externalRole: String,
    val cmsRole: CmsRole,
) {
    ADMIN("admin", "local-review-admin", "review-admin", CmsRole.ADMIN),
    SUPER("super", "local-review-super", "review-super", CmsRole.SUPER),
    ;

    companion object {
        fun fromToken(value: String): ReviewIdentityProfile? = entries.singleOrNull { it.token == value }
    }
}

data class ReviewIdentitySession(
    val credential: String,
    val expiresAt: Instant,
    val identity: AdminIdentityResponse,
)

class ReviewIdentitySessions(
    adminToken: String,
    superToken: String,
    private val sessionTtl: Duration,
    private val maxSessions: Int,
    private val clock: Clock = Clock.systemUTC(),
    private val random: SecureRandom = SecureRandom(),
) {
    private val configured = mapOf(
        adminToken.validToken("admin") to ReviewIdentityProfile.ADMIN,
        superToken.validToken("super") to ReviewIdentityProfile.SUPER,
    ).also { require(it.size == 2) { "Review identity tokens must be distinct" } }
    private val active = ConcurrentHashMap<String, ActiveReviewSession>()

    init {
        require(!sessionTtl.isZero && !sessionTtl.isNegative && sessionTtl <= Duration.ofHours(24)) {
            "Review identity session TTL must be between zero and 24 hours"
        }
        require(maxSessions in 1..10_000) { "Review identity max sessions must be between 1 and 10000" }
    }

    @Synchronized
    fun create(profile: ReviewIdentityProfile): ReviewIdentitySession {
        removeExpired()
        if (active.size >= maxSessions) throw ReviewIdentityCapacityException()
        val credential = generateCredential()
        val expiresAt = clock.instant().plus(sessionTtl)
        active[credential] = ActiveReviewSession(profile, expiresAt)
        return ReviewIdentitySession(
            credential = credential,
            expiresAt = expiresAt,
            identity = AdminIdentityResponse("local-review", profile.userId, listOf(profile.cmsRole.code)),
        )
    }

    fun verify(credential: String): VerifiedIdentity? {
        configured.entries.firstOrNull { constantTimeEquals(it.key, credential) }?.let { return it.value.verified() }
        val session = active[credential] ?: return null
        if (!session.expiresAt.isAfter(clock.instant())) {
            active.remove(credential, session)
            return null
        }
        return session.profile.verified()
    }

    fun invalidate(credential: String?) {
        if (!credential.isNullOrBlank()) active.remove(credential)
        removeExpired()
    }

    private fun removeExpired() {
        val now = clock.instant()
        active.entries.removeIf { !it.value.expiresAt.isAfter(now) }
    }

    private fun generateCredential(): String {
        repeat(8) {
            val bytes = ByteArray(32).also(random::nextBytes)
            val candidate = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
            if (!configured.containsKey(candidate) && !active.containsKey(candidate)) return candidate
        }
        error("Unable to allocate Review identity credential")
    }

    private data class ActiveReviewSession(val profile: ReviewIdentityProfile, val expiresAt: Instant)
}

@RestController
@RequestMapping("/api/review/identity/sessions")
class ReviewIdentitySessionController(
    private val sessions: ReviewIdentitySessions,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: CreateReviewIdentitySessionRequest,
        response: HttpServletResponse,
    ): ReviewIdentitySession {
        val profile = ReviewIdentityProfile.fromToken(request.profile.trim().lowercase())
            ?: throw InvalidReviewIdentityProfileException()
        return sessions.create(profile).also { session -> setResourceCookie(response, session.credential, session.expiresAt) }
    }

    @DeleteMapping("/current")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @RequestHeader(REVIEW_CREDENTIAL_HEADER, required = false) credential: String?,
        response: HttpServletResponse,
    ) {
        sessions.invalidate(credential)
        clearResourceCookie(response)
    }

    @PostMapping("/current/expire")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun expire(
        @RequestHeader(REVIEW_CREDENTIAL_HEADER, required = false) credential: String?,
        response: HttpServletResponse,
    ) {
        sessions.invalidate(credential)
        clearResourceCookie(response)
    }
}

data class CreateReviewIdentitySessionRequest(val profile: String = "")

@ResponseStatus(HttpStatus.BAD_REQUEST, reason = "测试身份类型无效")
class InvalidReviewIdentityProfileException : RuntimeException()

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE, reason = "测试身份会话已达到上限")
class ReviewIdentityCapacityException : RuntimeException()

private class ReviewCredentialVerifier(
    private val sessions: ReviewIdentitySessions,
) : CredentialVerifier {
    override val identitySource: String = "local-review"
    override fun verify(credential: String): VerifiedIdentity? = sessions.verify(credential)
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
        val credential = request.getHeader(REVIEW_CREDENTIAL_HEADER)?.takeIf { it.isNotBlank() }
            ?: request.resourcePreviewCredential()
        if (credential != null && SecurityContextHolder.getContext().authentication == null) {
            runCatching { converter.authenticate(identitySource, credential) }
                .getOrNull()
                ?.let { principal -> SecurityContextHolder.getContext().authentication = principal.toSpringAuthentication() }
        }
        filterChain.doFilter(request, response)
    }
}

private fun ReviewIdentityProfile.verified() = VerifiedIdentity(userId, setOf(externalRole))

private fun String.validToken(role: String): String = trim().also {
    require(it.isNotEmpty()) { "Review $role identity token must be explicitly configured" }
}

private fun constantTimeEquals(expected: String, actual: String): Boolean = MessageDigest.isEqual(
    expected.toByteArray(Charsets.UTF_8),
    actual.toByteArray(Charsets.UTF_8),
)

private fun HttpServletRequest.resourcePreviewCredential(): String? {
    val previewRequest = method == "GET" && requestURI.matches(Regex("^/api/admin/resources/[^/]+/content$"))
    return if (previewRequest) cookies?.firstOrNull { it.name == REVIEW_RESOURCE_COOKIE }?.value else null
}

private fun setResourceCookie(response: HttpServletResponse, credential: String, expiresAt: Instant) {
    val maxAge = Duration.between(Instant.now(), expiresAt).seconds.coerceAtLeast(1)
    response.addHeader(
        "Set-Cookie",
        "$REVIEW_RESOURCE_COOKIE=$credential; Max-Age=$maxAge; Path=/api/admin/resources/; HttpOnly; SameSite=Strict",
    )
}

private fun clearResourceCookie(response: HttpServletResponse) {
    response.addHeader(
        "Set-Cookie",
        "$REVIEW_RESOURCE_COOKIE=; Max-Age=0; Path=/api/admin/resources/; HttpOnly; SameSite=Strict",
    )
}
