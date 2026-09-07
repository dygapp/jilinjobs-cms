package com.jilinjobs.cms.siteconfig

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/site-config")
class AdminSiteConfigController(private val service: SiteConfigService) {
    @GetMapping fun list() = service.list()
    @GetMapping("/groups") fun groups() = service.groups()
    @PostMapping fun create(@RequestBody request: SaveSiteConfigRequest) = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.draft()))
    @PutMapping("/{key}") fun updateValue(@PathVariable key: String, @RequestBody request: SiteConfigUpdateRequest) = service.update(key, request.value)
    @PutMapping("/{key}/definition") fun updateDefinition(@PathVariable key: String, @RequestBody request: SaveSiteConfigRequest) = service.updateDefinition(key, request.draft())
    @DeleteMapping("/{key}") fun delete(@PathVariable key: String): ResponseEntity<Void> { service.delete(key); return ResponseEntity.noContent().build() }
}

@RestController
@RequestMapping("/api/public/site-config")
class PublicSiteConfigController(private val service: SiteConfigService) {
    @GetMapping fun list() = service.listPublic()
}

data class SiteConfigUpdateRequest(val value: String)

data class SaveSiteConfigRequest(
    val key: String,
    val name: String,
    val groupCode: String = "GENERAL",
    val value: String = "",
    val valueType: String = "TEXT",
    val description: String = "",
    val sortOrder: Int = 0,
    val required: Boolean = false,
    val system: Boolean = false,
    val enabled: Boolean = true,
) {
    fun draft() = SiteConfigDraft(key, name, groupCode, value, valueType, description, sortOrder, required, system, enabled)
}
