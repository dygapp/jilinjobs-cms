package com.jilinjobs.cms.column

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
@RequestMapping("/api/admin/columns")
class ColumnController(
    private val service: ColumnService,
) {
    @GetMapping
    fun list(): List<CmsColumn> = service.list()

    @PostMapping
    fun create(@Valid @RequestBody request: SaveColumnRequest): ResponseEntity<CmsColumn> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.toDraft()))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: SaveColumnRequest,
    ): CmsColumn = service.update(id, request.toDraft())

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}

data class SaveColumnRequest(
    val parentId: Long? = null,
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
) {
    fun toDraft(): ColumnDraft = ColumnDraft(
        parentId = parentId,
        name = name,
        sortOrder = sortOrder,
        enabled = enabled,
    )
}
