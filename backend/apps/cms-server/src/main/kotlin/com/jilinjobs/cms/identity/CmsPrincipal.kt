package com.jilinjobs.cms.identity

import java.util.Set.copyOf

enum class CmsRole(val code: String) {
    ADMIN("admin"),
    SUPER("super"),
}

data class CmsOperatorId(val identitySource: String, val userId: String)

/** A validated CMS identity. HTTP input must never be bound directly to this type. */
class CmsPrincipal private constructor(
    val identitySource: String,
    val userId: String,
    roles: Set<CmsRole>,
) {
    val operatorId: CmsOperatorId = CmsOperatorId(identitySource, userId)
    val roles: Set<CmsRole> = copyOf(roles)

    internal companion object {
        fun fromVerified(identitySource: String, userId: String, roles: Set<CmsRole>): CmsPrincipal =
            CmsPrincipal(identitySource, userId, roles)
    }
}
