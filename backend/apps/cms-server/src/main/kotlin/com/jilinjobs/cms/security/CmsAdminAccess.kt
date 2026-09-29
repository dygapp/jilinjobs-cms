package com.jilinjobs.cms.security

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@PreAuthorize("hasAnyAuthority('cms:admin','cms:super')")
annotation class CmsAdminAccess
