package com.jilinjobs.cms.listing

import com.jilinjobs.cms.common.ContentImagePolicy
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class PublicCmsListQueryRow(
    var listId: Long = 0,
    var listCode: String = "",
    var listName: String = "",
    var groupCode: String = "",
    var imagePolicy: String = "OPTIONAL",
    var itemId: Long? = null,
    var itemTitle: String? = null,
    var itemSubtitle: String? = null,
    var itemUrl: String? = null,
    var itemImagePath: String? = null,
    var itemOpenMode: String? = null,
    var itemSortOrder: Int? = null,
    var itemEnabled: Boolean? = null,
    var itemExtraJson: String? = null,
)

@Mapper
interface PublicCmsListQueryMapper {
    @Select(
        """
        SELECT l.id AS list_id, l.code AS list_code, l.name AS list_name,
               l.group_code, l.image_policy,
               i.id AS item_id, i.title AS item_title, i.subtitle AS item_subtitle,
               i.url AS item_url, i.image_path AS item_image_path,
               i.open_mode AS item_open_mode, i.sort_order AS item_sort_order,
               i.enabled AS item_enabled, i.extra_json AS item_extra_json
        FROM cms_list l
        LEFT JOIN cms_list_item i ON i.list_id = l.id AND i.enabled = 1
        WHERE l.enabled = 1 AND l.code = #{code}
        ORDER BY l.sort_order, l.id, i.sort_order, i.id
        """,
    )
    fun findByCode(@Param("code") code: String): List<PublicCmsListQueryRow>

    @Select(
        """
        SELECT l.id AS list_id, l.code AS list_code, l.name AS list_name,
               l.group_code, l.image_policy,
               i.id AS item_id, i.title AS item_title, i.subtitle AS item_subtitle,
               i.url AS item_url, i.image_path AS item_image_path,
               i.open_mode AS item_open_mode, i.sort_order AS item_sort_order,
               i.enabled AS item_enabled, i.extra_json AS item_extra_json
        FROM cms_list l
        LEFT JOIN cms_list_item i ON i.list_id = l.id AND i.enabled = 1
        WHERE l.enabled = 1 AND l.group_code = #{groupCode}
        ORDER BY l.sort_order, l.id, i.sort_order, i.id
        """,
    )
    fun findByGroup(@Param("groupCode") groupCode: String): List<PublicCmsListQueryRow>
}

@Service
class PublicCmsListQueryService(private val mapper: PublicCmsListQueryMapper) {
    @Transactional(readOnly = true)
    fun byCode(rawCode: String): PublicCmsList {
        val code = normalize(rawCode)
        val rows = mapper.findByCode(code)
        if (rows.isEmpty()) throw CmsListNotFoundException(code)
        return rows.toLists().single()
    }

    @Transactional(readOnly = true)
    fun byGroup(rawGroupCode: String): List<PublicCmsList> = mapper.findByGroup(normalize(rawGroupCode)).toLists()

    private fun normalize(value: String): String = value.trim().uppercase()

    private fun List<PublicCmsListQueryRow>.toLists(): List<PublicCmsList> =
        groupBy { it.listId }.values.map { rows ->
            val list = rows.first()
            PublicCmsList(
                id = list.listId,
                code = list.listCode,
                name = list.listName,
                groupCode = list.groupCode,
                imagePolicy = ContentImagePolicy.valueOf(list.imagePolicy),
                items = rows.mapNotNull { row ->
                    row.itemId?.let { id ->
                        CmsListItem(
                            id = id,
                            listId = row.listId,
                            title = row.itemTitle.orEmpty(),
                            subtitle = row.itemSubtitle,
                            url = row.itemUrl,
                            imagePath = row.itemImagePath,
                            openMode = row.itemOpenMode ?: "DEFAULT",
                            sortOrder = row.itemSortOrder ?: 0,
                            enabled = row.itemEnabled ?: true,
                            extraJson = row.itemExtraJson,
                        )
                    }
                },
            )
        }
}

@RestController
@RequestMapping("/api/public/lists")
class PublicCmsListQueryController(private val service: PublicCmsListQueryService) {
    @GetMapping("/by-code/{code}") fun byCode(@PathVariable code: String) = service.byCode(code)
    @GetMapping("/by-group/{groupCode}") fun byGroup(@PathVariable groupCode: String) = service.byGroup(groupCode)
}
