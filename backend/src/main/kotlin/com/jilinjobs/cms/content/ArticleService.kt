package com.jilinjobs.cms.content

import com.jilinjobs.cms.column.*
import com.jilinjobs.cms.resource.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ArticleService(private val repository:ArticleRepository,private val columnQuery:ColumnQuery,private val resourceAssociation:ArticleResourceAssociation){
 @Transactional(readOnly=true) fun list()=repository.findAll().map(::withResources)
 @Transactional(readOnly=true) fun get(id:Long)=withResources(repository.findById(id)?:throw ArticleNotFoundException(id))
 @Transactional fun create(draft:ArticleDraft):CmsArticle{val d=normalize(draft);val a=repository.insert(d);resourceAssociation.replaceArticleResources(a.id,d.links());return withResources(a)}
 @Transactional fun update(id:Long,draft:ArticleDraft):CmsArticle{repository.findById(id)?:throw ArticleNotFoundException(id);val d=normalize(draft);val a=repository.update(id,d);resourceAssociation.replaceArticleResources(id,d.links());return withResources(a)}
 @Transactional fun publish(id:Long):CmsArticle{val a=repository.findById(id)?:throw ArticleNotFoundException(id);if(a.status==ArticleStatus.PUBLISHED)throw ArticleValidationException("文章已经处于已发布状态");return withResources(repository.updateStatus(id,ArticleStatus.PUBLISHED,LocalDateTime.now()))}
 @Transactional fun withdraw(id:Long):CmsArticle{val a=repository.findById(id)?:throw ArticleNotFoundException(id);if(a.status!=ArticleStatus.PUBLISHED)throw ArticleValidationException("只有已发布文章可以撤回");return withResources(repository.updateStatus(id,ArticleStatus.WITHDRAWN,a.actualPublishedAt))}
 @Transactional(readOnly=true) fun listPublic(columnId:Long?,page:Int,size:Int):PublicArticlePage{if(page<0)throw ArticleValidationException("页码不能小于 0");if(size !in 1..50)throw ArticleValidationException("每页数量必须在 1 到 50 之间");columnId?.let{if(columnQuery.find(it)==null)throw ArticleValidationException("所属栏目不存在：$it")};val rows=repository.findPublished(columnId,size,page*size);return PublicArticlePage(rows.map(::summary),page,size,repository.countPublished(columnId))}
 @Transactional fun getPublic(id:Long):PublicArticleDetail{if(!repository.incrementPublishedViewCount(id))throw ArticleNotFoundException(id);val a=withResources(repository.findPublishedById(id)?:throw ArticleNotFoundException(id));val c=columnQuery.find(a.columnId)?:throw ArticleNotFoundException(id);return PublicArticleDetail(a.id,a.columnId,c.name,a.title,a.bodyHtml,a.source,a.publishDate,a.bodyImageResourceIds,resourceAssociation.findArticleAttachments(a.id).map{PublicArticleAttachment(it.id,it.originalFilename,it.contentType,it.sizeBytes)},c.alias)}
 private fun normalize(d:ArticleDraft):ArticleDraft{val t=d.title.trim();if(t.isBlank())throw ArticleValidationException("文章标题不能为空");if(t.length>200)throw ArticleValidationException("文章标题不能超过 200 个字符");if(d.source.length>200)throw ArticleValidationException("内容来源不能超过 200 个字符");if(columnQuery.find(d.columnId)==null)throw ArticleValidationException("所属栏目不存在：${d.columnId}");return d.copy(title=t,source=d.source.trim(),bodyImageResourceIds=d.bodyImageResourceIds.distinct(),attachmentResourceIds=d.attachmentResourceIds.distinct())}
 private fun summary(a:CmsArticle):PublicArticleSummary{val c=columnQuery.find(a.columnId);return PublicArticleSummary(a.id,a.columnId,c?.name?:"栏目 #${a.columnId}",a.title,a.publishDate,a.pinned,a.recommended,a.sortOrder,c?.alias.orEmpty())}
 private fun withResources(a:CmsArticle):CmsArticle{val l=resourceAssociation.findArticleResources(a.id);return a.copy(coverResourceId=l.coverResourceId,bodyImageResourceIds=l.bodyImageResourceIds,attachmentResourceIds=l.attachmentResourceIds)}
 private fun ArticleDraft.links()=ArticleResourceLinks(coverResourceId,bodyImageResourceIds,attachmentResourceIds)
}
@Component class ArticleColumnContentDependency(private val repository:ArticleRepository):ColumnContentDependency{override fun hasContent(columnId:Long)=repository.existsByColumn(columnId)}
