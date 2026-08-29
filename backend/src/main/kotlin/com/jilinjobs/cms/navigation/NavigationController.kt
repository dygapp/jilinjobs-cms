package com.jilinjobs.cms.navigation

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/navigations")
class AdminNavigationController(private val service: NavigationService) {
    @GetMapping fun list() = service.listAdmin()
    @PostMapping fun create(@Valid @RequestBody request: SaveNavigationRequest) =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.draft()))
    @PutMapping("/{id}") fun update(@PathVariable id: Long, @Valid @RequestBody request: SaveNavigationRequest) =
        service.update(id, request.draft())
    @DeleteMapping("/{id}") fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}

@RestController
@RequestMapping("/api/public/navigations")
class PublicNavigationController(private val service: NavigationService) {
    @GetMapping fun list() = service.listPublic()
}

data class SaveNavigationRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:NotBlank @field:Size(max = 50) val position: String,
    @field:Size(max = 100) val category: String? = null,
    val targetType: NavigationTargetType,
    val targetColumnId: Long? = null,
    @field:Size(max = 1000) val targetUrl: String? = null,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val parentId: Long? = null,
    val targetPageId: Long? = null,
    val openMode: NavigationOpenMode = NavigationOpenMode.DEFAULT,
) {
    fun draft() = NavigationDraft(name, position, category, targetType, targetColumnId, targetUrl, sortOrder, enabled, parentId, targetPageId, openMode)
}
