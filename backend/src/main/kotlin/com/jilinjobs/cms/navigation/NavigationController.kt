package com.jilinjobs.cms.navigation

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/navigations")
class AdminNavigationController(
    private val service: NavigationService,
) {
    @GetMapping
    fun list(): List<CmsNavigation> = service.listAdmin()

    @PostMapping
    fun create(@Valid @RequestBody request: SaveNavigationRequest): ResponseEntity<CmsNavigation> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.toDraft()))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: SaveNavigationRequest,
    ): CmsNavigation = service.update(id, request.toDraft())

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}

@RestController
@RequestMapping("/api/public/navigations")
class PublicNavigationController(
    private val service: NavigationService,
) {
    @GetMapping
    fun list(): List<PublicNavigation> = service.listPublic()
}

data class SaveNavigationRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
    val position: NavigationPosition,
    @field:Size(max = 100)
    val category: String? = null,
    val targetType: NavigationTargetType,
    val targetColumnId: Long? = null,
    @field:Size(max = 1000)
    val targetUrl: String? = null,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
) {
    fun toDraft(): NavigationDraft = NavigationDraft(
        name = name,
        position = position,
        category = category,
        targetType = targetType,
        targetColumnId = targetColumnId,
        targetUrl = targetUrl,
        sortOrder = sortOrder,
        enabled = enabled,
    )
}
