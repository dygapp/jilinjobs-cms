package com.jilinjobs.cms.listing

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Service
class PublicCmsListQueryService(private val service: CmsListService) {
    @Transactional(readOnly = true)
    fun byCode(rawCode: String): PublicCmsList = service.publicByCode(rawCode)

    @Transactional(readOnly = true)
    fun byGroup(rawGroupCode: String): List<PublicCmsList> = service.publicByGroup(rawGroupCode)
}

@RestController
@RequestMapping("/api/public/lists")
class PublicCmsListQueryController(private val service: PublicCmsListQueryService) {
    @GetMapping("/by-code/{code}") fun byCode(@PathVariable code: String) = service.byCode(code)
    @GetMapping("/by-group/{groupCode}") fun byGroup(@PathVariable groupCode: String) = service.byGroup(groupCode)
}
