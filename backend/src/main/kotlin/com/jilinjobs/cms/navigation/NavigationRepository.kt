package com.jilinjobs.cms.navigation

interface NavigationRepository {
    fun findAll(): List<CmsNavigation>

    fun findEnabled(): List<CmsNavigation>

    fun findById(id: Long): CmsNavigation?

    fun insert(draft: NavigationDraft): CmsNavigation

    fun update(id: Long, draft: NavigationDraft): CmsNavigation

    fun delete(id: Long)
}
