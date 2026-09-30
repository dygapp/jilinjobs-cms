package com.jilinjobs.cms.identity

import com.jilinjobs.cms.security.CmsAdminAccess
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AdminIdentityResponse(
    val identitySource: String,
    val userId: String,
    val roles: List<String>,
)

fun CmsPrincipal.toAdminIdentityResponse(): AdminIdentityResponse = AdminIdentityResponse(
    identitySource = identitySource,
    userId = userId,
    roles = roles.map(CmsRole::code).sorted(),
)

@CmsAdminAccess
@RestController
@RequestMapping("/api/admin/identity")
class AdminIdentityController {
    @GetMapping
    fun current(authentication: Authentication): AdminIdentityResponse {
        val principal = authentication.principal as? CmsPrincipal
            ?: throw AccessDeniedException("管理身份无访问权限")
        return principal.toAdminIdentityResponse()
    }
}
