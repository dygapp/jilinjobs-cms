package com.jilinjobs.cms.column

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/columns")
class PublicColumnController(
    private val columns: ColumnQuery,
) {
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): PublicColumn = columns.find(id)
        ?.let { PublicColumn(it.id, it.parentId, it.name) }
        ?: throw ColumnNotFoundException(id)
}

data class PublicColumn(
    val id: Long,
    val parentId: Long?,
    val name: String,
)
