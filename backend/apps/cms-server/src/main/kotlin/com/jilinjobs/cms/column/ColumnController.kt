package com.jilinjobs.cms.column

import com.jilinjobs.cms.common.ContentImagePolicy
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/columns")
class ColumnController(private val service: ColumnService) {
    @GetMapping fun list(): List<CmsColumn> = service.list()
    @PostMapping fun create(@Valid @RequestBody request: SaveColumnRequest): ResponseEntity<CmsColumn> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.toDraft()))
    @PutMapping("/{id}") fun update(@PathVariable id: Long, @Valid @RequestBody request: SaveColumnRequest): CmsColumn = service.update(id, request.toDraft())
    @DeleteMapping("/{id}") fun delete(@PathVariable id: Long): ResponseEntity<Void> { service.delete(id); return ResponseEntity.noContent().build() }
}

data class SaveColumnRequest(
    val parentId: Long? = null,
    @field:NotBlank @field:Size(max=100) val name: String,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    @field:Size(max=100) val alias: String = "",
    val coverPolicy: ContentImagePolicy = ContentImagePolicy.OPTIONAL,
) {
    fun toDraft() = ColumnDraft(parentId, name, sortOrder, enabled, alias, coverPolicy)
}
