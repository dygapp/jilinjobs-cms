package com.jilinjobs.cms.listing

import com.jilinjobs.cms.common.ContentImagePolicy
import com.jilinjobs.cms.content.ArticleRepository
import com.jilinjobs.cms.content.ArticleStatus
import com.jilinjobs.cms.content.ArticleType
import com.jilinjobs.cms.resource.ArticleResourceAssociation
import com.jilinjobs.cms.resource.ResourceService
import java.net.URI
import org.apache.ibatis.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.ObjectMapper

enum class CmsListItemSourceType { LINK, ARTICLE }

data class CmsListDefinition(
    val id: Long,
    val code: String,
    val name: String,
    val groupCode: String,
    val imagePolicy: ContentImagePolicy,
    val description: String,
    val sortOrder: Int,
    val enabled: Boolean,
    val system: Boolean,
    val preset: Boolean = false,
)

data class CmsListItem(
    val id: Long,
    val listId: Long,
    val sourceType: CmsListItemSourceType,
    val articleId: Long?,
    val articleType: ArticleType?,
    val articleStatus: ArticleStatus?,
    val title: String,
    val subtitle: String?,
    val url: String?,
    val imagePath: String?,
    val imageResourceId: Long?,
    val effectiveImageResourceId: Long?,
    val openMode: String,
    val sortOrder: Int,
    val enabled: Boolean,
    val extraJson: String?,
)

data class PublicCmsList(
    val id: Long,
    val code: String,
    val name: String,
    val groupCode: String,
    val imagePolicy: ContentImagePolicy,
    val items: List<CmsListItem>,
)

data class CmsListDraft(
    val code: String,
    val name: String,
    val groupCode: String = "GENERAL",
    val imagePolicy: ContentImagePolicy = ContentImagePolicy.OPTIONAL,
    val description: String = "",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val system: Boolean = false,
)

data class CmsListItemDraft(
    val sourceType: CmsListItemSourceType = CmsListItemSourceType.LINK,
    val articleId: Long? = null,
    val title: String = "",
    val subtitle: String? = null,
    val url: String? = null,
    val imagePath: String? = null,
    val imageResourceId: Long? = null,
    val openMode: String = "DEFAULT",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val extraJson: String? = null,
)

class CmsListValidationException(message: String) : RuntimeException(message)
class CmsListNotFoundException(value: String) : RuntimeException("通用列表不存在：$value")
class CmsListItemNotFoundException(id: Long) : RuntimeException("列表项不存在：$id")

data class CmsListRecord(
    var id: Long? = null,
    var code: String = "",
    var name: String = "",
    var groupCode: String = "GENERAL",
    var imagePolicy: String = "OPTIONAL",
    var description: String = "",
    var sortOrder: Int = 0,
    var enabled: Boolean = true,
    var systemFlag: Boolean = false,
    var preset: Boolean = false,
)

data class CmsListItemRecord(
    var id: Long? = null,
    var listId: Long = 0,
    var sourceType: String = CmsListItemSourceType.LINK.name,
    var articleId: Long? = null,
    var title: String = "",
    var subtitle: String? = null,
    var url: String? = null,
    var imagePath: String? = null,
    var imageResourceId: Long? = null,
    var openMode: String = "DEFAULT",
    var sortOrder: Int = 0,
    var enabled: Boolean = true,
    var extraJson: String? = null,
)

@Mapper
interface CmsListMapper {
    @Select("SELECT id,code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset FROM cms_list ORDER BY group_code,sort_order,id")
    fun findAll(): List<CmsListRecord>

    @Select("SELECT id,code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset FROM cms_list WHERE enabled=1 ORDER BY group_code,sort_order,id")
    fun findEnabled(): List<CmsListRecord>

    @Select("SELECT id,code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset FROM cms_list WHERE id=#{id}")
    fun findById(@Param("id") id: Long): CmsListRecord?

    @Select("SELECT id,code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset FROM cms_list WHERE code=#{code}")
    fun findByCode(@Param("code") code: String): CmsListRecord?

    @Insert("INSERT INTO cms_list(code,name,group_code,image_policy,description,sort_order,enabled,system_flag) VALUES(#{code},#{name},#{groupCode},#{imagePolicy},#{description},#{sortOrder},#{enabled},#{systemFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insertList(record: CmsListRecord): Int

    @Update("UPDATE cms_list SET name=#{name},group_code=#{groupCode},image_policy=#{imagePolicy},description=#{description},sort_order=#{sortOrder},enabled=#{enabled},system_flag=#{systemFlag} WHERE id=#{id}")
    fun updateList(record: CmsListRecord): Int

    @Delete("DELETE FROM cms_list WHERE id=#{id}")
    fun deleteList(@Param("id") id: Long): Int

    @Select("SELECT id,list_id,source_type,article_id,title,subtitle,url,image_path,image_resource_id,open_mode,sort_order,enabled,extra_json FROM cms_list_item WHERE list_id=#{listId} ORDER BY sort_order,id")
    fun findItems(@Param("listId") listId: Long): List<CmsListItemRecord>

    @Select("SELECT id,list_id,source_type,article_id,title,subtitle,url,image_path,image_resource_id,open_mode,sort_order,enabled,extra_json FROM cms_list_item WHERE list_id=#{listId} AND enabled=1 ORDER BY sort_order,id")
    fun findEnabledItems(@Param("listId") listId: Long): List<CmsListItemRecord>

    @Select("SELECT id,list_id,source_type,article_id,title,subtitle,url,image_path,image_resource_id,open_mode,sort_order,enabled,extra_json FROM cms_list_item WHERE id=#{id}")
    fun findItem(@Param("id") id: Long): CmsListItemRecord?

    @Insert("INSERT INTO cms_list_item(list_id,source_type,article_id,title,subtitle,url,image_path,image_resource_id,open_mode,sort_order,enabled,extra_json) VALUES(#{listId},#{sourceType},#{articleId},#{title},#{subtitle},#{url},#{imagePath},#{imageResourceId},#{openMode},#{sortOrder},#{enabled},#{extraJson})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insertItem(record: CmsListItemRecord): Int

    @Update("UPDATE cms_list_item SET source_type=#{sourceType},article_id=#{articleId},title=#{title},subtitle=#{subtitle},url=#{url},image_path=#{imagePath},image_resource_id=#{imageResourceId},open_mode=#{openMode},sort_order=#{sortOrder},enabled=#{enabled},extra_json=#{extraJson} WHERE id=#{id}")
    fun updateItem(record: CmsListItemRecord): Int

    @Delete("DELETE FROM cms_list_item WHERE id=#{id}")
    fun deleteItem(@Param("id") id: Long): Int

    @Select("SELECT image_path FROM cms_list_item i JOIN cms_list l ON l.id=i.list_id WHERE l.enabled=1 AND i.enabled=1 AND i.source_type='LINK' AND i.image_path LIKE '/static/%'")
    fun findReferencedImages(): List<String>
}

@Service
class CmsListService(
    private val mapper: CmsListMapper,
    private val objectMapper: ObjectMapper,
    private val articleRepository: ArticleRepository,
    private val articleResources: ArticleResourceAssociation,
    private val resourceService: ResourceService,
) {
    private val openModes = setOf("DEFAULT", "SAME_WINDOW", "NEW_WINDOW")

    @Transactional(readOnly = true)
    fun listDefinitions() = mapper.findAll().map { it.model() }

    @Transactional(readOnly = true)
    fun listItems(listId: Long): List<CmsListItem> {
        val list = requireList(listId)
        val policy = ContentImagePolicy.valueOf(list.imagePolicy)
        return mapper.findItems(listId).map { resolveItem(it, policy) }
    }

    @Transactional(readOnly = true)
    fun publicLists(): List<PublicCmsList> = mapper.findEnabled().map(::publicList)

    @Transactional(readOnly = true)
    fun publicByCode(rawCode: String): PublicCmsList {
        val code = rawCode.trim().uppercase()
        val record = mapper.findByCode(code)?.takeIf { it.enabled } ?: throw CmsListNotFoundException(code)
        return publicList(record)
    }

    @Transactional(readOnly = true)
    fun publicByGroup(rawGroupCode: String): List<PublicCmsList> {
        val groupCode = rawGroupCode.trim().uppercase()
        return mapper.findEnabled().filter { it.groupCode == groupCode }.map(::publicList)
    }

    @Transactional
    fun createList(draft: CmsListDraft): CmsListDefinition {
        val normalized = normalizeList(draft)
        if (mapper.findByCode(normalized.code) != null) throw CmsListValidationException("列表 Code 已存在：${normalized.code}")
        val record = normalized.record()
        mapper.insertList(record)
        return mapper.findById(requireNotNull(record.id))!!.model()
    }

    @Transactional
    fun updateList(id: Long, draft: CmsListDraft): CmsListDefinition {
        val current = requireList(id)
        val normalized = normalizeList(draft.copy(code = current.code))
        validateExistingItemsForPolicy(id, normalized.imagePolicy)
        mapper.updateList(normalized.record(id))
        return mapper.findById(id)!!.model()
    }

    @Transactional
    fun deleteList(id: Long) {
        val current = requireList(id)
        if (current.preset) throw CmsListValidationException("预置列表属于网站规划基线，不能删除")
        mapper.deleteList(id)
    }

    @Transactional
    fun createItem(listId: Long, draft: CmsListItemDraft): CmsListItem {
        val list = requireList(listId)
        val policy = ContentImagePolicy.valueOf(list.imagePolicy)
        val normalized = normalizeItem(draft, policy)
        val record = normalized.record(listId)
        mapper.insertItem(record)
        return resolveItem(record, policy)
    }

    @Transactional
    fun updateItem(listId: Long, id: Long, draft: CmsListItemDraft): CmsListItem {
        val list = requireList(listId)
        val current = mapper.findItem(id) ?: throw CmsListItemNotFoundException(id)
        if (current.listId != listId) throw CmsListValidationException("列表项不属于当前列表")
        val currentSourceType = CmsListItemSourceType.valueOf(current.sourceType)
        if (currentSourceType != draft.sourceType) throw CmsListValidationException("列表项数据类型创建后不可修改")
        if (currentSourceType == CmsListItemSourceType.ARTICLE && current.articleId != draft.articleId) {
            throw CmsListValidationException("文章型列表项的关联文章创建后不可修改")
        }
        val policy = ContentImagePolicy.valueOf(list.imagePolicy)
        val normalized = normalizeItem(draft, policy)
        mapper.updateItem(normalized.record(listId, id))
        return resolveItem(mapper.findItem(id)!!, policy)
    }

    @Transactional
    fun deleteItem(listId: Long, id: Long) {
        requireList(listId)
        val current = mapper.findItem(id) ?: throw CmsListItemNotFoundException(id)
        if (current.listId != listId) throw CmsListValidationException("列表项不属于当前列表")
        mapper.deleteItem(id)
    }

    private fun publicList(record: CmsListRecord): PublicCmsList {
        val policy = ContentImagePolicy.valueOf(record.imagePolicy)
        return PublicCmsList(
            requireNotNull(record.id),
            record.code,
            record.name,
            record.groupCode,
            policy,
            mapper.findEnabledItems(requireNotNull(record.id)).mapNotNull { row ->
                val item = resolveItem(row, policy)
                if (item.sourceType == CmsListItemSourceType.ARTICLE && item.articleStatus != ArticleStatus.PUBLISHED) {
                    return@mapNotNull null
                }
                if (policy == ContentImagePolicy.REQUIRED) {
                    val hasEffectiveImage = when (item.sourceType) {
                        CmsListItemSourceType.LINK -> !item.imagePath.isNullOrBlank()
                        CmsListItemSourceType.ARTICLE -> item.effectiveImageResourceId != null
                    }
                    if (!hasEffectiveImage) return@mapNotNull null
                }
                item
            },
        )
    }

    private fun requireList(id: Long) = mapper.findById(id) ?: throw CmsListNotFoundException(id.toString())

    private fun normalizeList(draft: CmsListDraft): CmsListDraft {
        val code = draft.code.trim().uppercase()
        if (!code.matches(Regex("[A-Z][A-Z0-9_]{1,99}"))) throw CmsListValidationException("列表 Code 格式不正确")
        val name = draft.name.trim()
        if (name.isBlank()) throw CmsListValidationException("列表名称不能为空")
        val group = draft.groupCode.trim().uppercase().ifBlank { "GENERAL" }
        if (!group.matches(Regex("[A-Z][A-Z0-9_]{1,49}"))) throw CmsListValidationException("列表分组 Code 格式不正确")
        return draft.copy(code = code, name = name, groupCode = group, description = draft.description.trim())
    }

    private fun normalizeItem(draft: CmsListItemDraft, policy: ContentImagePolicy): CmsListItemDraft {
        val mode = draft.openMode.trim().uppercase()
        if (mode !in openModes) throw CmsListValidationException("列表项打开方式不正确")
        val extra = draft.extraJson?.trim()?.takeIf { it.isNotBlank() }
        if (extra != null) runCatching { objectMapper.readTree(extra) }.getOrElse { throw CmsListValidationException("列表项扩展数据必须是合法 JSON") }

        return when (draft.sourceType) {
            CmsListItemSourceType.LINK -> {
                val title = draft.title.trim()
                if (title.isBlank()) throw CmsListValidationException("链接型列表项标题不能为空")
                val url = draft.url?.trim()?.takeIf { it.isNotBlank() }
                val image = draft.imagePath?.trim()?.takeIf { it.isNotBlank() }
                if (url != null) validateUrl(url)
                if (image != null && !image.startsWith("/static/")) throw CmsListValidationException("链接型列表项图片必须使用 /static/ 资源路径")
                validateLinkImagePolicy(policy, image)
                draft.copy(
                    articleId = null,
                    title = title,
                    subtitle = draft.subtitle?.trim()?.takeIf { it.isNotBlank() },
                    url = url,
                    imagePath = image,
                    imageResourceId = null,
                    openMode = mode,
                    extraJson = extra,
                )
            }
            CmsListItemSourceType.ARTICLE -> {
                val articleId = draft.articleId ?: throw CmsListValidationException("文章型列表项必须选择文章")
                val article = articleRepository.findById(articleId) ?: throw CmsListValidationException("关联文章不存在：$articleId")
                val overrideResourceId = draft.imageResourceId
                if (overrideResourceId != null) {
                    val resource = runCatching { resourceService.get(overrideResourceId) }.getOrElse {
                        throw CmsListValidationException("列表项图片资源不存在：$overrideResourceId")
                    }
                    if (resource.contentType?.startsWith("image/") != true) throw CmsListValidationException("列表项覆盖资源必须是图片")
                }
                val coverResourceId = articleResources.findArticleResources(article.id).coverResourceId
                when (policy) {
                    ContentImagePolicy.NONE -> if (overrideResourceId != null) throw CmsListValidationException("当前列表不使用图片")
                    ContentImagePolicy.REQUIRED -> if (overrideResourceId == null && coverResourceId == null) {
                        throw CmsListValidationException("当前列表要求图片；关联文章没有主题图片，请从正文图片中选择或上传轮播图片")
                    }
                    ContentImagePolicy.OPTIONAL -> Unit
                }
                draft.copy(
                    articleId = article.id,
                    title = article.title,
                    subtitle = draft.subtitle?.trim()?.takeIf { it.isNotBlank() },
                    url = null,
                    imagePath = null,
                    imageResourceId = overrideResourceId,
                    openMode = mode,
                    extraJson = extra,
                )
            }
        }
    }

    private fun validateLinkImagePolicy(policy: ContentImagePolicy, imagePath: String?) {
        when (policy) {
            ContentImagePolicy.NONE -> if (imagePath != null) throw CmsListValidationException("当前列表不使用图片")
            ContentImagePolicy.REQUIRED -> if (imagePath == null) throw CmsListValidationException("当前列表要求每个链接型列表项设置图片")
            ContentImagePolicy.OPTIONAL -> Unit
        }
    }

    private fun validateExistingItemsForPolicy(listId: Long, policy: ContentImagePolicy) {
        val items = mapper.findItems(listId)
        when (policy) {
            ContentImagePolicy.NONE -> if (items.any { row ->
                row.sourceType == CmsListItemSourceType.LINK.name && !row.imagePath.isNullOrBlank() ||
                    row.sourceType == CmsListItemSourceType.ARTICLE.name && row.imageResourceId != null
            }) {
                throw CmsListValidationException("当前列表仍有列表项保存图片，清除列表项图片后才能改为“不使用图片”")
            }
            ContentImagePolicy.REQUIRED -> if (items.any { row ->
                val type = CmsListItemSourceType.valueOf(row.sourceType)
                when (type) {
                    CmsListItemSourceType.LINK -> row.imagePath.isNullOrBlank()
                    CmsListItemSourceType.ARTICLE -> {
                        val articleId = row.articleId
                        val cover = articleId?.let { articleResources.findArticleResources(it).coverResourceId }
                        row.imageResourceId == null && cover == null
                    }
                }
            }) {
                throw CmsListValidationException("当前列表仍有列表项缺少有效图片，补齐后才能改为“图片必填”")
            }
            ContentImagePolicy.OPTIONAL -> Unit
        }
    }

    private fun resolveItem(record: CmsListItemRecord, policy: ContentImagePolicy): CmsListItem {
        val sourceType = CmsListItemSourceType.valueOf(record.sourceType)
        if (sourceType == CmsListItemSourceType.LINK) {
            return CmsListItem(
                id = requireNotNull(record.id),
                listId = record.listId,
                sourceType = sourceType,
                articleId = null,
                articleType = null,
                articleStatus = null,
                title = record.title,
                subtitle = record.subtitle,
                url = record.url,
                imagePath = record.imagePath,
                imageResourceId = null,
                effectiveImageResourceId = null,
                openMode = record.openMode,
                sortOrder = record.sortOrder,
                enabled = record.enabled,
                extraJson = record.extraJson,
            )
        }
        val articleId = record.articleId ?: throw CmsListValidationException("文章型列表项缺少 articleId：${record.id}")
        val article = articleRepository.findById(articleId) ?: throw CmsListValidationException("关联文章不存在：$articleId")
        val coverResourceId = articleResources.findArticleResources(articleId).coverResourceId
        val effectiveImage = if (policy == ContentImagePolicy.NONE) null else record.imageResourceId ?: coverResourceId
        return CmsListItem(
            id = requireNotNull(record.id),
            listId = record.listId,
            sourceType = sourceType,
            articleId = article.id,
            articleType = article.articleType,
            articleStatus = article.status,
            title = article.title,
            subtitle = record.subtitle,
            url = if (article.articleType == ArticleType.EXTERNAL_LINK) article.externalUrl else null,
            imagePath = null,
            imageResourceId = record.imageResourceId,
            effectiveImageResourceId = effectiveImage,
            openMode = record.openMode,
            sortOrder = record.sortOrder,
            enabled = record.enabled,
            extraJson = record.extraJson,
        )
    }

    private fun validateUrl(value: String) {
        if (value.startsWith("/") && !value.startsWith("//")) return
        val uri = runCatching { URI(value) }.getOrElse { throw CmsListValidationException("列表项 URL 格式不正确") }
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            throw CmsListValidationException("列表项 URL 必须是站内路径或 HTTP(S) 地址")
        }
    }

    private fun CmsListDraft.record(id: Long? = null) = CmsListRecord(id, code, name, groupCode, imagePolicy.name, description, sortOrder, enabled, system)
    private fun CmsListItemDraft.record(listId: Long, id: Long? = null) = CmsListItemRecord(
        id = id,
        listId = listId,
        sourceType = sourceType.name,
        articleId = articleId,
        title = title,
        subtitle = subtitle,
        url = url,
        imagePath = imagePath,
        imageResourceId = imageResourceId,
        openMode = openMode,
        sortOrder = sortOrder,
        enabled = enabled,
        extraJson = extraJson,
    )
    private fun CmsListRecord.model() = CmsListDefinition(requireNotNull(id), code, name, groupCode, ContentImagePolicy.valueOf(imagePolicy), description, sortOrder, enabled, systemFlag, preset)
}

@RestController
@RequestMapping("/api/admin/lists")
class AdminCmsListController(private val service: CmsListService) {
    @GetMapping fun lists() = service.listDefinitions()
    @PostMapping fun createList(@RequestBody request: SaveCmsListRequest) = ResponseEntity.status(HttpStatus.CREATED).body(service.createList(request.draft()))
    @PutMapping("/{id}") fun updateList(@PathVariable id: Long, @RequestBody request: SaveCmsListRequest) = service.updateList(id, request.draft())
    @DeleteMapping("/{id}") fun deleteList(@PathVariable id: Long): ResponseEntity<Void> { service.deleteList(id); return ResponseEntity.noContent().build() }
    @GetMapping("/{id}/items") fun items(@PathVariable id: Long) = service.listItems(id)
    @PostMapping("/{id}/items") fun createItem(@PathVariable id: Long, @RequestBody request: SaveCmsListItemRequest) = ResponseEntity.status(HttpStatus.CREATED).body(service.createItem(id, request.draft()))
    @PutMapping("/{listId}/items/{itemId}") fun updateItem(@PathVariable listId: Long, @PathVariable itemId: Long, @RequestBody request: SaveCmsListItemRequest) = service.updateItem(listId, itemId, request.draft())
    @DeleteMapping("/{listId}/items/{itemId}") fun deleteItem(@PathVariable listId: Long, @PathVariable itemId: Long): ResponseEntity<Void> { service.deleteItem(listId, itemId); return ResponseEntity.noContent().build() }
}

@RestController
@RequestMapping("/api/public/lists")
class PublicCmsListController(private val service: CmsListService) {
    @GetMapping fun lists() = service.publicLists()
}

data class SaveCmsListRequest(
    val code: String,
    val name: String,
    val groupCode: String = "GENERAL",
    val imagePolicy: ContentImagePolicy = ContentImagePolicy.OPTIONAL,
    val description: String = "",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val system: Boolean = false,
) {
    fun draft() = CmsListDraft(code, name, groupCode, imagePolicy, description, sortOrder, enabled, system)
}

data class SaveCmsListItemRequest(
    val sourceType: CmsListItemSourceType = CmsListItemSourceType.LINK,
    val articleId: Long? = null,
    val title: String = "",
    val subtitle: String? = null,
    val url: String? = null,
    val imagePath: String? = null,
    val imageResourceId: Long? = null,
    val openMode: String = "DEFAULT",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val extraJson: String? = null,
) {
    fun draft() = CmsListItemDraft(sourceType, articleId, title, subtitle, url, imagePath, imageResourceId, openMode, sortOrder, enabled, extraJson)
}
