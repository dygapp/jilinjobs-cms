package com.jilinjobs.cms.listing

import com.jilinjobs.cms.common.ContentImagePolicy
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
