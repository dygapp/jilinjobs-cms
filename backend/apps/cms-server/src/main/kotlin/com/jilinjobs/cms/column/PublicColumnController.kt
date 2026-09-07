package com.jilinjobs.cms.column

import com.jilinjobs.cms.common.ContentImagePolicy
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/public/columns")
class PublicColumnController(private val columns: ColumnQuery) {
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): PublicColumn = columns.find(id)?.toPublic() ?: throw ColumnNotFoundException(id)

    @GetMapping("/by-alias/{alias}")
    fun getByAlias(@PathVariable alias: String): PublicColumn = columns.findByAlias(alias)?.toPublic() ?: throw ColumnAliasNotFoundException(alias)

    private fun CmsColumn.toPublic() = PublicColumn(id, parentId, name, alias, coverPolicy)
}

data class PublicColumn(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val alias: String,
    val coverPolicy: ContentImagePolicy,
)
