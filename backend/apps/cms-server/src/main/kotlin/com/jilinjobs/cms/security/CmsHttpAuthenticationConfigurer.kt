package com.jilinjobs.cms.security

import org.springframework.security.config.annotation.web.builders.HttpSecurity

/** Provider-specific authentication adapters extend the Server request boundary through this seam. */
fun interface CmsHttpAuthenticationConfigurer {
    fun configure(http: HttpSecurity)
}
