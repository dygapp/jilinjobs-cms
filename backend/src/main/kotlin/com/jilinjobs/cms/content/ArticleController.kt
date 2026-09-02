package com.jilinjobs.cms.content

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/admin/articles")
class ArticleController(private val service: ArticleService) {
    @GetMapping fun list(): List<CmsArticle> = service.list()
    @GetMapping("/{id}") fun get(@PathVariable id: Long): CmsArticle = service.get(id)
    @PostMapping fun create(@Valid @RequestBody request: SaveArticleRequest): ResponseEntity<CmsArticle> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.toDraft()))
    @PutMapping("/{id}") fun update(@PathVariable id: Long, @Valid @RequestBody request: SaveArticleRequest): CmsArticle = service.update(id, request.toDraft())
    @PostMapping("/{id}/publish") fun publish(@PathVariable id: Long): CmsArticle = service.publish(id)
    @PostMapping("/{id}/withdraw") fun withdraw(@PathVariable id: Long): CmsArticle = service.withdraw(id)
}

@RestController
@RequestMapping("/api/public/articles")
class PublicArticleController(
    private val service: ArticleService,
    private val summaryQuery: PublicArticleSummaryQueryService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) columnId: Long?,
        @RequestParam(required = false) articleType: ArticleType?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PublicArticlePage = summaryQuery.list(columnId, articleType, page, size)

    @GetMapping("/{id}") fun get(@PathVariable id: Long): PublicArticleDetail = service.getPublic(id)
}

data class SaveArticleRequest(
    val columnId: Long,
    @field:NotBlank @field:Size(max = 200) val title: String,
    val bodyHtml: String = "",
    @field:Size(max = 200) val source: String = "",
    val articleType: ArticleType = ArticleType.INTERNAL,
    @field:Size(max = 2000) val externalUrl: String? = null,
    val publishDate: LocalDate? = null,
    val pinned: Boolean = false,
    val recommended: Boolean = false,
    val sortOrder: Int = 0,
    val coverResourceId: Long? = null,
    val bodyImageResourceIds: List<Long> = emptyList(),
    val attachmentResourceIds: List<Long> = emptyList(),
) {
    fun toDraft() = ArticleDraft(
        columnId = columnId,
        title = title,
        bodyHtml = bodyHtml,
        source = source,
        articleType = articleType,
        externalUrl = externalUrl,
        publishDate = publishDate,
        pinned = pinned,
        recommended = recommended,
        sortOrder = sortOrder,
        coverResourceId = coverResourceId,
        bodyImageResourceIds = bodyImageResourceIds,
        attachmentResourceIds = attachmentResourceIds,
    )
}
