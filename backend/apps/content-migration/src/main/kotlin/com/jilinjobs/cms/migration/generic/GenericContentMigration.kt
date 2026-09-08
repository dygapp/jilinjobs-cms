package com.jilinjobs.cms.migration.generic

import com.jilinjobs.cms.ContentMigrationApplication
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.content.ArticleDraft
import com.jilinjobs.cms.content.ArticleService
import com.jilinjobs.cms.content.ArticleType
import com.jilinjobs.cms.listing.CmsListItemDraft
import com.jilinjobs.cms.listing.CmsListItemSourceType
import com.jilinjobs.cms.listing.CmsListService
import com.jilinjobs.cms.migration.ArticleLegacyMappingMapper
import com.jilinjobs.cms.migration.ArticleLegacyMappingRecord
import com.jilinjobs.cms.migration.CmsListItemLegacyMappingMapper
import com.jilinjobs.cms.migration.CmsListItemLegacyMappingRecord
import com.jilinjobs.cms.resource.ResourceService
import com.jilinjobs.cms.staticresource.StaticResourceNotFoundException
import com.jilinjobs.cms.staticresource.StaticResourceService
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.LocalDate
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.ObjectMapper

private val SHA256 = Regex("[0-9a-f]{64}")
private val MIGRATION_TOKEN = Regex("migration-(resource|attachment)://[0-9a-f]{64}")
private val CANONICAL_ASSET_REFERENCE = Regex("""(?i)(?:src|href)=[\"'](assets/[^\"']+)[\"']""")
private val OPEN_MODES = setOf("DEFAULT", "SAME_WINDOW", "NEW_WINDOW")
private val STATIC_IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp", "ico")

data class CanonicalArticleSource(
    val system: String,
    val legacyKey: String,
    val contentId: String? = null,
    val typeCode: String,
    val detailPath: String,
    val url: String,
)

data class CanonicalArticleTarget(
    val columnAlias: String,
    val articleType: ArticleType,
)

data class CanonicalArticleContent(
    val title: String,
    val source: String,
    val publishDate: LocalDate?,
    val bodyHtml: String,
    val externalUrl: String?,
)

data class CanonicalMigrationResource(
    val role: String,
    val sourceUrl: String,
    val originalReference: String,
    val token: String,
    val snapshotPath: String,
    val sha256: String,
    val contentType: String? = null,
    val sizeBytes: Long,
)

data class CanonicalArticleEvidence(
    val listPage: Int,
    val listTitle: String,
    val listPublishDate: LocalDate?,
    val sourceOrder: Int,
    val detailPublishDate: LocalDate? = null,
    val rawDetailPath: String? = null,
)

data class CanonicalArticleRecord(
    val source: CanonicalArticleSource,
    val target: CanonicalArticleTarget,
    val content: CanonicalArticleContent,
    val resources: List<CanonicalMigrationResource>,
    val sourceFingerprint: String,
    val evidence: CanonicalArticleEvidence,
)

data class CanonicalArticleIndexEntry(
    val legacyKey: String,
    val path: String,
)

data class CanonicalListItemReference(
    val legacyKey: String,
    val path: String,
    val sourceOrder: Int,
    val sourceFingerprint: String,
)

data class CanonicalListIndex(
    val listCode: String,
    val sourceSystem: String,
    val sourcePage: String? = null,
    val items: List<CanonicalListItemReference>,
)

data class CanonicalArticleReference(
    val sourceSystem: String,
    val legacyKey: String,
)

data class CanonicalListImage(
    val sourceUrl: String,
    val snapshotPath: String,
    val sha256: String,
    val contentType: String? = null,
    val sizeBytes: Long,
)

data class CanonicalListItemRecord(
    val legacyKey: String,
    val sourceOrder: Int,
    val sourceType: CmsListItemSourceType,
    val title: String,
    val subtitle: String? = null,
    val url: String? = null,
    val articleReference: CanonicalArticleReference? = null,
    val openMode: String = "DEFAULT",
    val enabled: Boolean = true,
    val sourceFingerprint: String,
    val image: CanonicalListImage? = null,
)

enum class GenericMigrationStatus { CREATED, SKIPPED, CONFLICT, INVALID }
enum class GenericMigrationPhase { PREFLIGHT, EXECUTE }
enum class GenericMigrationKind { ARTICLE, LIST_ITEM, DATASET }

data class GenericMigrationResult(
    val kind: GenericMigrationKind,
    val sourceSystem: String,
    val legacyKey: String,
    val status: GenericMigrationStatus,
    val runtimeId: Long? = null,
    val message: String? = null,
)

data class GenericContentMigrationReport(
    val phase: GenericMigrationPhase,
    val total: Int,
    val created: Int,
    val skipped: Int,
    val conflicts: Int,
    val invalid: Int,
    val results: List<GenericMigrationResult>,
)

data class LoadedResource(
    val canonical: CanonicalMigrationResource,
    val file: Path,
)

data class LoadedArticle(
    val record: CanonicalArticleRecord,
    val resources: List<LoadedResource>,
)

data class LoadedListImage(
    val canonical: CanonicalListImage,
    val file: Path,
)

data class LoadedListItem(
    val listCode: String,
    val sourceSystem: String,
    val sourcePage: String?,
    val record: CanonicalListItemRecord,
    val image: LoadedListImage?,
)

data class LoadedDataset(
    val articles: List<LoadedArticle>,
    val listItems: List<LoadedListItem>,
) {
    val total: Int get() = articles.size + listItems.size
}

enum class PlanAction { CREATE, SKIP }

data class ArticlePlan(
    val loaded: LoadedArticle,
    val columnId: Long,
    val action: PlanAction,
    val existingArticleId: Long? = null,
)

data class ListItemPlan(
    val loaded: LoadedListItem,
    val listId: Long,
    val action: PlanAction,
    val existingListItemId: Long? = null,
)

data class GenericImportPlan(
    val total: Int,
    val articles: List<ArticlePlan>,
    val listItems: List<ListItemPlan>,
)

data class PreflightOutcome(
    val plan: GenericImportPlan?,
    val report: GenericContentMigrationReport?,
)

@Service
class CanonicalDatasetLoader(
    private val objectMapper: ObjectMapper,
) {
    fun load(snapshotRoot: Path): LoadedDataset {
        val root = snapshotRoot.toAbsolutePath().normalize()
        require(Files.isDirectory(root)) { "Canonical snapshot root 不存在：$root" }

        val articles = loadArticles(root)
        val listItems = loadListItems(root)
        require(articles.isNotEmpty() || listItems.isNotEmpty()) { "Canonical snapshot 至少需要一个 Article 或 ListItem" }

        requireUniqueArticleIdentities(articles)
        requireUniqueListIdentities(listItems)
        return LoadedDataset(
            articles = articles.sortedWith(compareBy<LoadedArticle> { it.record.evidence.sourceOrder }.thenBy { identity(it.record.source.system, it.record.source.legacyKey) }),
            listItems = listItems.sortedWith(compareBy<LoadedListItem> { it.record.sourceOrder }.thenBy { identity(it.sourceSystem, it.record.legacyKey) }),
        )
    }

    private fun loadArticles(root: Path): List<LoadedArticle> {
        val indexFile = root.resolve("index.ndjson")
        if (!Files.isRegularFile(indexFile)) return emptyList()
        val loaded = mutableListOf<LoadedArticle>()
        Files.newBufferedReader(indexFile).useLines { lines ->
            lines.filter(String::isNotBlank).forEachIndexed { index, line ->
                val entry = runCatching { objectMapper.readValue(line, CanonicalArticleIndexEntry::class.java) }
                    .getOrElse { throw IllegalArgumentException("Article index 第 ${index + 1} 行无法解析：${it.message}") }
                val articleFile = resolveRegularFile(root, entry.path, "Article index path")
                val record = Files.newBufferedReader(articleFile).use { reader ->
                    runCatching { objectMapper.readValue(reader, CanonicalArticleRecord::class.java) }
                        .getOrElse { throw IllegalArgumentException("Canonical Article 无法解析：${entry.path}: ${it.message}") }
                }
                require(record.source.legacyKey == entry.legacyKey) { "Article index legacyKey 与 article.json 不一致：${entry.legacyKey}" }
                validateArticleShape(record)
                val articleRoot = articleFile.parent
                val resources = record.resources.map { resource ->
                    validateResourceShape(resource)
                    LoadedResource(resource, verifyFile(articleRoot, resource.snapshotPath, resource.sizeBytes, resource.sha256, "Article resource"))
                }
                validateArticleReferences(record)
                loaded += LoadedArticle(record, resources)
            }
        }
        return loaded
    }

    private fun loadListItems(root: Path): List<LoadedListItem> {
        val listsRoot = root.resolve("lists")
        if (!Files.isDirectory(listsRoot)) return emptyList()
        return Files.list(listsRoot).use { stream ->
            stream.filter(Files::isDirectory)
                .sorted()
                .flatMap { listRoot ->
                    val indexFile = listRoot.resolve("index.json")
                    require(Files.isRegularFile(indexFile)) { "List canonical directory 缺少 index.json：${root.relativize(listRoot)}" }
                    val index = Files.newBufferedReader(indexFile).use { reader ->
                        runCatching { objectMapper.readValue(reader, CanonicalListIndex::class.java) }
                            .getOrElse { throw IllegalArgumentException("List index 无法解析：${root.relativize(indexFile)}: ${it.message}") }
                    }
                    validateListIndex(index)
                    require(listRoot.fileName.toString().equals(index.listCode, ignoreCase = true)) {
                        "List directory 与 listCode 不一致：${listRoot.fileName} / ${index.listCode}"
                    }
                    index.items.sortedWith(compareBy<CanonicalListItemReference> { it.sourceOrder }.thenBy { it.legacyKey }).stream().map { reference ->
                        val itemFile = resolveRegularFile(listRoot, reference.path, "List item path")
                        val record = Files.newBufferedReader(itemFile).use { reader ->
                            runCatching { objectMapper.readValue(reader, CanonicalListItemRecord::class.java) }
                                .getOrElse { throw IllegalArgumentException("Canonical ListItem 无法解析：${root.relativize(itemFile)}: ${it.message}") }
                        }
                        require(record.legacyKey == reference.legacyKey) { "List index legacyKey 与 item.json 不一致：${reference.legacyKey}" }
                        require(record.sourceOrder == reference.sourceOrder) { "List index sourceOrder 与 item.json 不一致：${reference.legacyKey}" }
                        require(record.sourceFingerprint == reference.sourceFingerprint) { "List index fingerprint 与 item.json 不一致：${reference.legacyKey}" }
                        validateListItemShape(index.sourceSystem, record)
                        val image = record.image?.let { canonical ->
                            validateListImageShape(canonical)
                            val file = verifyFile(itemFile.parent, canonical.snapshotPath, canonical.sizeBytes, canonical.sha256, "List image")
                            validateStaticImageBytes(file, canonical.snapshotPath)
                            LoadedListImage(canonical, file)
                        }
                        LoadedListItem(index.listCode, index.sourceSystem, index.sourcePage, record, image)
                    }
                }
                .toList()
        }
    }

    private fun validateArticleShape(record: CanonicalArticleRecord) {
        require(record.source.system.isNotBlank() && record.source.system.length <= 100) { "Article source system 不合法" }
        require(record.source.legacyKey.isNotBlank() && record.source.legacyKey.length <= 255) { "Article legacy identity 不合法" }
        require(record.source.typeCode.isNotBlank() && record.source.typeCode.length <= 100) { "Article typeCode 不合法" }
        require(record.source.detailPath.isNotBlank() && record.source.detailPath.length <= 500) { "Article detailPath 不合法" }
        validateHttpUrl(record.source.url, "Article source URL")
        require(record.sourceFingerprint.matches(SHA256)) { "Article source fingerprint 不合法：${record.source.legacyKey}" }
        require(record.target.columnAlias.isNotBlank() && record.target.columnAlias.length <= 100) { "Article target Column alias 不合法" }
        require(record.content.title.isNotBlank() && record.content.title.length <= 200) { "Article title 不合法" }
        require(record.content.source.length <= 200) { "Article content source 过长" }
        require(record.evidence.sourceOrder > 0) { "Article sourceOrder 必须大于 0" }
        when (record.target.articleType) {
            ArticleType.INTERNAL -> require(record.content.externalUrl == null) { "INTERNAL Article 不能携带 externalUrl" }
            ArticleType.EXTERNAL_LINK -> {
                require(record.content.bodyHtml.isEmpty() && record.resources.isEmpty()) { "EXTERNAL_LINK Article 不应包含正文或资源" }
                validateHttpUrl(requireNotNull(record.content.externalUrl) { "EXTERNAL_LINK URL 不能为空" }, "EXTERNAL_LINK URL")
            }
        }
    }

    private fun validateResourceShape(resource: CanonicalMigrationResource) {
        require(resource.role in setOf("BODY_IMAGE", "ATTACHMENT")) { "未知 Article resource role：${resource.role}" }
        require(resource.sha256.matches(SHA256)) { "Article resource SHA-256 不合法：${resource.snapshotPath}" }
        require(resource.sizeBytes >= 0) { "Article resource size 不能为负数" }
        require(resource.snapshotPath.isNotBlank()) { "Article resource snapshotPath 不能为空" }
        require(resource.originalReference.isNotBlank()) { "Article resource originalReference 不能为空" }
        validateHttpUrl(resource.sourceUrl, "Article resource source URL")
        val expectedToken = when (resource.role) {
            "BODY_IMAGE" -> "migration-resource://${resource.sha256}"
            "ATTACHMENT" -> "migration-attachment://${resource.sha256}"
            else -> error("unreachable")
        }
        require(resource.token == expectedToken) { "Article resource token 与 role/SHA-256 不一致" }
    }

    private fun validateArticleReferences(record: CanonicalArticleRecord) {
        if (record.target.articleType != ArticleType.INTERNAL) return
        val tokens = record.resources.map { it.token }.toSet()
        MIGRATION_TOKEN.findAll(record.content.bodyHtml).forEach { match ->
            require(match.value in tokens) { "Article body 存在未声明 migration token：${match.value}" }
        }
        val paths = record.resources.map { it.snapshotPath.replace('\\', '/') }.toSet()
        CANONICAL_ASSET_REFERENCE.findAll(record.content.bodyHtml).forEach { match ->
            require(match.groupValues[1] in paths) { "Article body 存在未声明 canonical asset reference：${match.groupValues[1]}" }
        }
    }

    private fun validateListIndex(index: CanonicalListIndex) {
        require(index.listCode.matches(Regex("[A-Z][A-Z0-9_]{1,99}"))) { "List code 不合法：${index.listCode}" }
        require(index.sourceSystem.isNotBlank() && index.sourceSystem.length <= 100) { "List source system 不合法" }
        val keys = index.items.map { it.legacyKey }
        require(keys.size == keys.toSet().size) { "List index 存在重复 legacy identity：${index.listCode}" }
        index.items.forEach { reference ->
            require(reference.legacyKey.isNotBlank() && reference.legacyKey.length <= 255) { "List item legacy identity 不合法" }
            require(reference.sourceOrder > 0) { "List item sourceOrder 必须大于 0" }
            require(reference.sourceFingerprint.matches(SHA256)) { "List item fingerprint 不合法：${reference.legacyKey}" }
        }
    }

    private fun validateListItemShape(sourceSystem: String, record: CanonicalListItemRecord) {
        require(sourceSystem.isNotBlank() && sourceSystem.length <= 100) { "List source system 不合法" }
        require(record.legacyKey.isNotBlank() && record.legacyKey.length <= 255) { "List item legacy identity 不合法" }
        require(record.sourceOrder > 0) { "List item sourceOrder 必须大于 0" }
        require(record.title.isNotBlank() && record.title.length <= 200) { "List item title 不合法" }
        require(record.openMode.uppercase() in OPEN_MODES) { "List item openMode 不合法：${record.openMode}" }
        require(record.sourceFingerprint.matches(SHA256)) { "List item fingerprint 不合法：${record.legacyKey}" }
        when (record.sourceType) {
            CmsListItemSourceType.LINK -> {
                require(record.articleReference == null) { "LINK ListItem 不能携带 articleReference" }
                validateHttpUrl(requireNotNull(record.url) { "LINK ListItem URL 不能为空" }, "LINK ListItem URL")
            }
            CmsListItemSourceType.ARTICLE -> {
                require(record.url == null) { "ARTICLE ListItem 不应携带 LINK URL" }
                val reference = requireNotNull(record.articleReference) { "ARTICLE ListItem 必须携带 stable articleReference" }
                require(reference.sourceSystem.isNotBlank() && reference.sourceSystem.length <= 100) { "ARTICLE reference sourceSystem 不合法" }
                require(reference.legacyKey.isNotBlank() && reference.legacyKey.length <= 255) { "ARTICLE reference legacyKey 不合法" }
            }
        }
    }

    private fun validateListImageShape(image: CanonicalListImage) {
        require(image.sha256.matches(SHA256)) { "List image SHA-256 不合法：${image.snapshotPath}" }
        require(image.sizeBytes > 0) { "List image 不能为空" }
        require(image.contentType?.startsWith("image/") == true) { "List image contentType 必须为 image/*" }
        validateHttpUrl(image.sourceUrl, "List image source URL")
        val extension = image.snapshotPath.substringAfterLast('.', "").lowercase()
        require(extension in STATIC_IMAGE_EXTENSIONS) { "List image 扩展名不支持：$extension" }
    }

    private fun requireUniqueArticleIdentities(articles: List<LoadedArticle>) {
        val identities = articles.map { identity(it.record.source.system, it.record.source.legacyKey) }
        require(identities.size == identities.toSet().size) { "Canonical Article dataset 存在重复 stable identity" }
    }

    private fun requireUniqueListIdentities(items: List<LoadedListItem>) {
        val identities = items.map { identity(it.sourceSystem, it.record.legacyKey) }
        require(identities.size == identities.toSet().size) { "Canonical ListItem dataset 存在重复 stable identity" }
    }

    private fun resolveRegularFile(root: Path, raw: String, label: String): Path {
        require(raw.isNotBlank()) { "$label 不能为空" }
        val resolved = root.resolve(raw).normalize()
        require(resolved.startsWith(root) && Files.isRegularFile(resolved)) { "$label 不存在或路径越界：$raw" }
        return resolved
    }

    private fun verifyFile(root: Path, raw: String, expectedSize: Long, expectedSha256: String, label: String): Path {
        val file = resolveRegularFile(root.toAbsolutePath().normalize(), raw, label)
        require(Files.size(file) == expectedSize) { "$label size 不一致：$raw" }
        require(sha256(file) == expectedSha256) { "$label SHA-256 不一致：$raw" }
        return file
    }

    private fun validateStaticImageBytes(file: Path, raw: String) {
        val extension = raw.substringAfterLast('.', "").lowercase()
        val header = Files.newInputStream(file).use { it.readNBytes(16) }
        val matches = when (extension) {
            "png" -> header.startsWith(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a))
            "jpg", "jpeg" -> header.size >= 2 && header[0] == 0xff.toByte() && header[1] == 0xd8.toByte()
            "gif" -> header.asAscii(6) in setOf("GIF87a", "GIF89a")
            "webp" -> header.asAscii(4) == "RIFF" && header.drop(8).take(4).toByteArray().toString(StandardCharsets.US_ASCII) == "WEBP"
            "ico" -> header.startsWith(byteArrayOf(0x00, 0x00, 0x01, 0x00))
            else -> false
        }
        require(matches) { "List image 实际内容与扩展名不一致：$raw" }
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean = size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
    private fun ByteArray.asAscii(length: Int): String = take(length).toByteArray().toString(StandardCharsets.US_ASCII)
}

@Service
class CanonicalMigrationPreflight(
    private val columnQuery: ColumnQuery,
    private val listService: CmsListService,
    private val articleMapping: ArticleLegacyMappingMapper,
    private val listItemMapping: CmsListItemLegacyMappingMapper,
    private val staticResourceService: StaticResourceService,
) {
    fun preflight(dataset: LoadedDataset): PreflightOutcome {
        val failures = mutableListOf<GenericMigrationResult>()
        val articlePlans = dataset.articles.mapNotNull { loaded ->
            val record = loaded.record
            val column = columnQuery.findByAlias(record.target.columnAlias)
            if (column == null || !column.enabled) {
                failures += invalid(GenericMigrationKind.ARTICLE, record.source.system, record.source.legacyKey, "目标 Column 不存在或已停用：${record.target.columnAlias}")
                null
            } else {
                val existing = articleMapping.find(record.source.system, record.source.legacyKey)
                when {
                    existing == null -> ArticlePlan(loaded, column.id, PlanAction.CREATE)
                    existing.sourceFingerprint == record.sourceFingerprint -> ArticlePlan(loaded, column.id, PlanAction.SKIP, existing.articleId)
                    else -> {
                        failures += conflict(GenericMigrationKind.ARTICLE, record.source.system, record.source.legacyKey, existing.articleId, "Article stable identity 已存在，但 source fingerprint 已变化")
                        null
                    }
                }
            }
        }

        val plannedArticleIdentities = articlePlans.associateBy { identity(it.loaded.record.source.system, it.loaded.record.source.legacyKey) }
        val definitions = listService.listDefinitions().associateBy { it.code }
        val listPlans = dataset.listItems.mapNotNull { loaded ->
            val record = loaded.record
            val definition = definitions[loaded.listCode]
            if (definition == null || !definition.enabled) {
                failures += invalid(GenericMigrationKind.LIST_ITEM, loaded.sourceSystem, record.legacyKey, "目标 List 不存在或已停用：${loaded.listCode}")
                return@mapNotNull null
            }
            if (record.sourceType == CmsListItemSourceType.ARTICLE) {
                val reference = requireNotNull(record.articleReference)
                val referenceKey = identity(reference.sourceSystem, reference.legacyKey)
                if (referenceKey !in plannedArticleIdentities && articleMapping.find(reference.sourceSystem, reference.legacyKey) == null) {
                    failures += invalid(GenericMigrationKind.LIST_ITEM, loaded.sourceSystem, record.legacyKey, "ARTICLE dependency 无法解析：${reference.sourceSystem}/${reference.legacyKey}")
                    return@mapNotNull null
                }
            }
            if (record.sourceType == CmsListItemSourceType.LINK && loaded.image != null) {
                val target = staticTarget(loaded.listCode, loaded.image.canonical)
                val existingFile = try {
                    staticResourceService.resolvePublic(target)
                } catch (_: StaticResourceNotFoundException) {
                    null
                }
                if (existingFile != null && sha256(existingFile) != loaded.image.canonical.sha256) {
                    failures += conflict(GenericMigrationKind.LIST_ITEM, loaded.sourceSystem, record.legacyKey, null, "Generic static image target 已存在但 bytes 不一致：$target")
                    return@mapNotNull null
                }
            }
            val existing = listItemMapping.find(loaded.sourceSystem, record.legacyKey)
            when {
                existing == null -> ListItemPlan(loaded, definition.id, PlanAction.CREATE)
                existing.sourceFingerprint == record.sourceFingerprint -> ListItemPlan(loaded, definition.id, PlanAction.SKIP, existing.listItemId)
                else -> {
                    failures += conflict(GenericMigrationKind.LIST_ITEM, loaded.sourceSystem, record.legacyKey, existing.listItemId, "ListItem stable identity 已存在，但 source fingerprint 已变化")
                    null
                }
            }
        }

        if (failures.isNotEmpty()) {
            return PreflightOutcome(null, report(GenericMigrationPhase.PREFLIGHT, dataset.total, failures))
        }
        return PreflightOutcome(GenericImportPlan(dataset.total, articlePlans, listPlans), null)
    }
}

@Service
class GenericArticleImporter(
    private val resourceService: ResourceService,
    private val articleService: ArticleService,
    private val mappingMapper: ArticleLegacyMappingMapper,
) {
    @Transactional
    fun execute(plan: ArticlePlan): GenericMigrationResult {
        val record = plan.loaded.record
        if (plan.action == PlanAction.SKIP) {
            return GenericMigrationResult(GenericMigrationKind.ARTICLE, record.source.system, record.source.legacyKey, GenericMigrationStatus.SKIPPED, plan.existingArticleId)
        }

        val bodyImages = mutableListOf<Long>()
        val attachments = mutableListOf<Long>()
        var bodyHtml = record.content.bodyHtml
        plan.loaded.resources.distinctBy { it.canonical.token }.forEach { loaded ->
            val resource = loaded.canonical
            val uploaded = resourceService.upload(PathMultipartFile(sourceFilename(resource.sourceUrl, resource.sha256, resource.snapshotPath), resource.contentType, loaded.file))
            when (resource.role) {
                "BODY_IMAGE" -> {
                    bodyImages += uploaded.id
                    val runtimePath = "/api/admin/resources/${uploaded.id}/content"
                    bodyHtml = bodyHtml.replace(resource.token, runtimePath).replace(resource.snapshotPath, runtimePath)
                }
                "ATTACHMENT" -> {
                    attachments += uploaded.id
                    val runtimePath = "/api/public/resources/${uploaded.id}/attachment"
                    bodyHtml = bodyHtml.replace(resource.token, runtimePath).replace(resource.snapshotPath, runtimePath)
                }
            }
        }
        require(!MIGRATION_TOKEN.containsMatchIn(bodyHtml) && !CANONICAL_ASSET_REFERENCE.containsMatchIn(bodyHtml)) {
            "Article execute 后仍存在未解析 canonical resource reference"
        }
        val created = articleService.create(
            ArticleDraft(
                columnId = plan.columnId,
                title = record.content.title,
                bodyHtml = bodyHtml,
                source = record.content.source,
                articleType = record.target.articleType,
                externalUrl = record.content.externalUrl,
                publishDate = record.content.publishDate,
                pinned = false,
                sortOrder = -record.evidence.sourceOrder,
                coverResourceId = null,
                bodyImageResourceIds = bodyImages,
                attachmentResourceIds = attachments,
            ),
        )
        val published = articleService.publish(created.id)
        mappingMapper.insert(
            ArticleLegacyMappingRecord(
                sourceSystem = record.source.system,
                legacyKey = record.source.legacyKey,
                contentId = record.source.contentId,
                typeCode = record.source.typeCode,
                detailPath = record.source.detailPath,
                sourceUrl = record.source.url,
                sourceFingerprint = record.sourceFingerprint,
                articleId = published.id,
            ),
        )
        return GenericMigrationResult(GenericMigrationKind.ARTICLE, record.source.system, record.source.legacyKey, GenericMigrationStatus.CREATED, published.id)
    }
}

@Service
class GenericListItemImporter(
    private val listService: CmsListService,
    private val staticResourceService: StaticResourceService,
    private val resourceService: ResourceService,
    private val articleMapping: ArticleLegacyMappingMapper,
    private val listItemMapping: CmsListItemLegacyMappingMapper,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(plan: ListItemPlan): GenericMigrationResult {
        val loaded = plan.loaded
        val record = loaded.record
        if (plan.action == PlanAction.SKIP) {
            return GenericMigrationResult(GenericMigrationKind.LIST_ITEM, loaded.sourceSystem, record.legacyKey, GenericMigrationStatus.SKIPPED, plan.existingListItemId)
        }

        var imagePath: String? = null
        var imageResourceId: Long? = null
        val image = loaded.image
        if (image != null) {
            when (record.sourceType) {
                CmsListItemSourceType.LINK -> {
                    val target = staticTarget(loaded.listCode, image.canonical)
                    val existing = try {
                        staticResourceService.resolvePublic(target)
                    } catch (_: StaticResourceNotFoundException) {
                        null
                    }
                    if (existing == null) {
                        staticResourceService.upload(
                            target,
                            PathMultipartFile(sourceFilename(image.canonical.sourceUrl, image.canonical.sha256, image.canonical.snapshotPath), image.canonical.contentType, image.file),
                            false,
                        )
                    } else {
                        require(sha256(existing) == image.canonical.sha256) { "Static image target bytes changed after preflight：$target" }
                    }
                    imagePath = "/static/$target"
                }
                CmsListItemSourceType.ARTICLE -> {
                    imageResourceId = resourceService.upload(
                        PathMultipartFile(sourceFilename(image.canonical.sourceUrl, image.canonical.sha256, image.canonical.snapshotPath), image.canonical.contentType, image.file),
                    ).id
                }
            }
        }

        val articleId = if (record.sourceType == CmsListItemSourceType.ARTICLE) {
            val reference = requireNotNull(record.articleReference)
            articleMapping.find(reference.sourceSystem, reference.legacyKey)?.articleId
                ?: error("ARTICLE dependency mapping 在 execute 时不存在：${reference.sourceSystem}/${reference.legacyKey}")
        } else {
            null
        }
        val extraJson = objectMapper.writeValueAsString(
            mapOf(
                "migrationSourceSystem" to loaded.sourceSystem,
                "migrationSourcePage" to loaded.sourcePage,
                "legacyKey" to record.legacyKey,
                "sourceFingerprint" to record.sourceFingerprint,
                "imageSourceUrl" to image?.canonical?.sourceUrl,
                "imageSha256" to image?.canonical?.sha256,
            ),
        )
        val created = listService.createItem(
            plan.listId,
            CmsListItemDraft(
                sourceType = record.sourceType,
                articleId = articleId,
                title = record.title,
                subtitle = record.subtitle,
                url = record.url,
                imagePath = imagePath,
                imageResourceId = imageResourceId,
                openMode = record.openMode,
                sortOrder = record.sourceOrder,
                enabled = record.enabled,
                extraJson = extraJson,
            ),
        )
        listItemMapping.insert(
            CmsListItemLegacyMappingRecord(
                sourceSystem = loaded.sourceSystem,
                legacyKey = record.legacyKey,
                sourceUrl = record.url.orEmpty(),
                sourceFingerprint = record.sourceFingerprint,
                imageSourceUrl = image?.canonical?.sourceUrl.orEmpty(),
                imageSha256 = image?.canonical?.sha256.orEmpty(),
                listItemId = created.id,
            ),
        )
        return GenericMigrationResult(GenericMigrationKind.LIST_ITEM, loaded.sourceSystem, record.legacyKey, GenericMigrationStatus.CREATED, created.id)
    }
}

@Service
class GenericContentMigrationService(
    private val loader: CanonicalDatasetLoader,
    private val preflight: CanonicalMigrationPreflight,
    private val articleImporter: GenericArticleImporter,
    private val listItemImporter: GenericListItemImporter,
) {
    fun importSnapshot(snapshotRoot: Path): GenericContentMigrationReport {
        val dataset = runCatching { loader.load(snapshotRoot) }.getOrElse { error ->
            return report(
                GenericMigrationPhase.PREFLIGHT,
                0,
                listOf(invalid(GenericMigrationKind.DATASET, "dataset", "snapshot", error.message ?: error::class.java.simpleName)),
            )
        }
        val outcome = preflight.preflight(dataset)
        outcome.report?.let { return it }
        val plan = requireNotNull(outcome.plan)
        val results = mutableListOf<GenericMigrationResult>()
        try {
            plan.articles.forEach { results += articleImporter.execute(it) }
            plan.listItems.forEach { results += listItemImporter.execute(it) }
        } catch (error: RuntimeException) {
            results += invalid(GenericMigrationKind.DATASET, "dataset", "execute", error.message ?: error::class.java.simpleName)
        }
        return report(GenericMigrationPhase.EXECUTE, plan.total, results)
    }
}

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "用法：importCanonicalContent <snapshot-root> [Spring Boot args...]" }
    val snapshotRoot = Path.of(args.first()).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(*args.drop(1).toTypedArray())
    try {
        val report = context.getBean(GenericContentMigrationService::class.java).importSnapshot(snapshotRoot)
        println("CONTENT_MIGRATION_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
        require(report.conflicts == 0 && report.invalid == 0) { "Generic content migration 存在 conflict/invalid，拒绝静默完成" }
    } finally {
        context.close()
    }
}

private fun report(
    phase: GenericMigrationPhase,
    total: Int,
    results: List<GenericMigrationResult>,
) = GenericContentMigrationReport(
    phase = phase,
    total = total,
    created = results.count { it.status == GenericMigrationStatus.CREATED },
    skipped = results.count { it.status == GenericMigrationStatus.SKIPPED },
    conflicts = results.count { it.status == GenericMigrationStatus.CONFLICT },
    invalid = results.count { it.status == GenericMigrationStatus.INVALID },
    results = results,
)

private fun invalid(kind: GenericMigrationKind, sourceSystem: String, legacyKey: String, message: String) =
    GenericMigrationResult(kind, sourceSystem, legacyKey, GenericMigrationStatus.INVALID, message = message)

private fun conflict(kind: GenericMigrationKind, sourceSystem: String, legacyKey: String, runtimeId: Long?, message: String) =
    GenericMigrationResult(kind, sourceSystem, legacyKey, GenericMigrationStatus.CONFLICT, runtimeId, message)

private fun identity(sourceSystem: String, legacyKey: String) = "$sourceSystem\u0000$legacyKey"

private fun sha256(path: Path): String = Files.newInputStream(path).use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(8192)
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        digest.update(buffer, 0, read)
    }
    digest.digest().joinToString("") { "%02x".format(it) }
}

private fun validateHttpUrl(value: String, label: String) {
    require(value.length <= 2000) { "$label 过长" }
    val uri = runCatching { URI(value) }.getOrNull()
    require(uri != null && uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank()) { "$label 不合法" }
}

private fun staticTarget(listCode: String, image: CanonicalListImage): String {
    val extension = image.snapshotPath.substringAfterLast('.', "").lowercase()
    require(extension in STATIC_IMAGE_EXTENSIONS) { "List image 扩展名不支持：$extension" }
    return "migrated/content/lists/${listCode.uppercase()}/${image.sha256}.$extension"
}

private fun sourceFilename(sourceUrl: String, sha: String, snapshotPath: String): String {
    val fromUrl = runCatching { Path.of(URI(sourceUrl).path).fileName?.toString() }.getOrNull().orEmpty()
    val fromSnapshot = runCatching { Path.of(snapshotPath).fileName?.toString() }.getOrNull().orEmpty()
    return fromUrl.ifBlank { fromSnapshot }.ifBlank { "$sha.bin" }.takeLast(255)
}

private class PathMultipartFile(
    private val originalFilename: String,
    private val contentType: String?,
    private val path: Path,
) : MultipartFile {
    override fun getName(): String = "file"
    override fun getOriginalFilename(): String = originalFilename
    override fun getContentType(): String? = contentType
    override fun isEmpty(): Boolean = Files.size(path) == 0L
    override fun getSize(): Long = Files.size(path)
    override fun getBytes(): ByteArray = Files.readAllBytes(path)
    override fun getInputStream(): InputStream = Files.newInputStream(path)
    override fun transferTo(dest: File) { Files.copy(path, dest.toPath(), StandardCopyOption.REPLACE_EXISTING) }
}
