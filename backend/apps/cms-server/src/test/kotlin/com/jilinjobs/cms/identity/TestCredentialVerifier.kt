package com.jilinjobs.cms.identity

import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/** Test-source-only adapter; no credentials or endpoints are packaged in the server application. */
class TestCredentialVerifier(
    override val identitySource: String,
    private val clock: Clock = Clock.systemUTC(),
) : CredentialVerifier {
    private data class IssuedCredential(val identity: VerifiedIdentity, val expiresAt: Instant)

    private val random = SecureRandom()
    private val issued = ConcurrentHashMap<String, IssuedCredential>()

    fun issue(userId: String, externalRoles: Set<String>, expiresAt: Instant): String {
        val bytes = ByteArray(32).also(random::nextBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        issued[token] = IssuedCredential(VerifiedIdentity(userId, externalRoles.toSet()), expiresAt)
        return token
    }

    override fun verify(credential: String): VerifiedIdentity? = issued[credential]
        ?.takeIf { clock.instant().isBefore(it.expiresAt) }
        ?.identity
}
