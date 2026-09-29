package com.jilinjobs.cms.listing

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PublicCmsListQueryService(private val service: CmsListService) {
    @Transactional(readOnly = true)
    fun byCode(rawCode: String): PublicCmsList = service.publicByCode(rawCode)

    @Transactional(readOnly = true)
    fun byGroup(rawGroupCode: String): List<PublicCmsList> = service.publicByGroup(rawGroupCode)
}
