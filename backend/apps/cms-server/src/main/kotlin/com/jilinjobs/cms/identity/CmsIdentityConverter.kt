package com.jilinjobs.cms.identity

/** Only a trusted server-side adapter may return this result after verifying a credential. */
data class VerifiedIdentity(val userId: String, val externalRoles: Set<String>)

interface CredentialVerifier {
    /** Stable, server-controlled identity source; never copied from a request claim. */
    val identitySource: String

    /** Returns null for rejected credentials. Implementations must verify issuer, target, expiry and integrity. */
    fun verify(credential: String): VerifiedIdentity?
}

class CmsAuthenticationException : RuntimeException("管理身份无法验证")

/** Converts a verified provider identity through server-controlled CMS role mappings. */
class CmsIdentityConverter(
    verifiers: Collection<CredentialVerifier>,
    roleMappings: Map<String, Map<String, CmsRole>>,
) {
    private val verifiersBySource: Map<String, CredentialVerifier> = verifiers.associateBy { verifier ->
        require(verifier.identitySource.isNotBlank()) { "身份来源不能为空" }
        verifier.identitySource
    }.also { require(it.size == verifiers.size) { "身份来源不能重复" } }

    private val mappings: Map<String, Map<String, CmsRole>> = roleMappings.mapValues { (_, roles) -> roles.toMap() }

    fun authenticate(sourceHint: String, credential: String): CmsPrincipal {
        val verifier = verifiersBySource[sourceHint] ?: deny()
        if (credential.isBlank()) deny()
        val verified = try {
            verifier.verify(credential)
        } catch (_: Exception) {
            null
        } ?: deny()
        if (verified.userId.isBlank() || verified.externalRoles.isEmpty()) deny()

        val sourceMappings = mappings[verifier.identitySource] ?: deny()
        val cmsRoles = verified.externalRoles.map { role -> sourceMappings[role] ?: deny() }.toSet()
        if (cmsRoles.isEmpty()) deny()
        return CmsPrincipal.fromVerified(verifier.identitySource, verified.userId, cmsRoles)
    }

    private fun deny(): Nothing = throw CmsAuthenticationException()
}
