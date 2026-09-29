package com.jilinjobs.cms.security

import com.jilinjobs.cms.identity.CmsPrincipal
import com.jilinjobs.cms.identity.CmsRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

object CmsAuthorities {
    const val ADMIN = "cms:admin"
    const val SUPER = "cms:super"

    fun forRole(role: CmsRole): String = when (role) {
        CmsRole.ADMIN -> ADMIN
        CmsRole.SUPER -> SUPER
    }
}

fun CmsPrincipal.toSpringAuthentication(): Authentication =
    UsernamePasswordAuthenticationToken.authenticated(
        this,
        null,
        roles.map { role -> SimpleGrantedAuthority(CmsAuthorities.forRole(role)) },
    )
