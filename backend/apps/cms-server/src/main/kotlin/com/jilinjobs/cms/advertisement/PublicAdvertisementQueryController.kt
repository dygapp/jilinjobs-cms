package com.jilinjobs.cms.advertisement

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/advertisements")
class PublicAdvertisementQueryController(private val service: PublicAdvertisementQueryService) {
    @GetMapping("/slots/{code}") fun byCode(@PathVariable code: String) = service.byCode(code)
}
