package com.jilinjobs.cms.migration

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.content.ArticleDraft
import com.jilinjobs.cms.content.ArticleService
import com.jilinjobs.cms.content.ArticleType
import com.jilinjobs.cms.resource.ResourceService
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.ObjectMapper
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

private const val PARTY_THEME_EDUCATION_ALIAS = "party-theme-education"
private val EU30_SHA256 = Regex("[0-9a-f]{64}")
private val EU30_MIGRATION_TOKEN = Regex("migration-(resource|attachment)://[0-9a-f]{64}")
private val EU30_CANONICAL_ASSET_REFERENCE = Regex("""(?i)(?:src|href)=[\"']assets/[^\"']+[\"']""")

/**
 * EU-30 extends the accepted EU-29 canonical dataset with one historical Party column.
 * Existing four-column records continue to use the frozen EU-29 importer; only the new
 * `party-theme-education` records use this extension importer. This keeps the accepted
 * EU-29 semantics stable while allowing the canonical index to grow without using a
 * Runtime article id in repository data.
 */
@Service
class PartyThemeEducationRecordImporter(
    private val mappingMapper: ArticleLegacyMappingMapper,
    private val columnQuery: ColumnQuery,
    private val resourceService: ResourceService,
    private val articleService: ArticleService,
) {
    @Transactional
    fun importRecord(snapshotRoot: Path, record: PartyMigrationRecord): PartyRecordImportResult {
        validateRecord(record)
        val existing = mappingMapper.find(record.source.system, record.source.legacyKey)
        if (existing != null) {
            return if (existing.sourceFingerprint == record.sourceFingerprint) {
                PartyRecordImportResult(record.source.legacyKey, PartyRecordImportStatus.SKIPPED, existing.articleId)
            } else {
                PartyRecordImportResult(
                    record.source.legacyKey,
                    PartyRecordImportStatus.CONFLICT,
                    existing.articleId,
                    "legacy identity 已导入，但 source fingerprint 已变化",
                )
            }
        }

        val column = columnQuery.findByAlias(record.target.columnAlias)
            ?: return PartyRecordImportResult(record.source.legacyKey, PartyRecordImportStatus.INVALID, message = "目标栏目不存在：${record.target.columnAlias}")
        if (!column.enabled) return PartyRecordImportResult(record.source.legacyKey, PartyRecordImportStatus.INVALID, message = "目标栏目已停用：${record.target.columnAlias}")

        // Canonical resources preserve source-reference occurrences. Validate every occurrence
        // first, then collapse identical stable tokens for Runtime storage/Article association.
        // This preserves source evidence without creating duplicate CmsResource rows.
        val verifiedResources = record.resources
            .map { resource -> resource to verifiedSnapshotFile(snapshotRoot, resource) }
            .distinctBy { (resource, _) -> resource.token }
        var bodyHtml = record.content.bodyHtml
        val bodyImageIds = mutableListOf<Long>()
        val attachmentIds = mutableListOf<Long>()
        for ((resource, file) in verifiedResources) {
            val uploaded = resourceService.upload(ThemeSnapshotMultipartFile(sourceFilename(resource), resource.contentType, file))
            when (resource.role) {
                "BODY_IMAGE" -> {
                    bodyImageIds += uploaded.id
                    val runtimePath = "/api/admin/resources/${uploaded.id}/content"
                    bodyHtml = bodyHtml.replace(resource.token, runtimePath).replace(resource.snapshotPath, runtimePath)
                }
                "ATTACHMENT" -> {
                    attachmentIds += uploaded.id
                    val runtimePath = "/api/public/resources/${uploaded.id}/attachment"
                    bodyHtml = bodyHtml.replace(resource.token, runtimePath).replace(resource.snapshotPath, runtimePath)
                }
            }
        }
        if (EU30_MIGRATION_TOKEN.containsMatchIn(bodyHtml) || EU30_CANONICAL_ASSET_REFERENCE.containsMatchIn(bodyHtml)) {
            return PartyRecordImportResult(record.source.legacyKey, PartyRecordImportStatus.INVALID, message = "正文仍存在未解析迁移资源引用")
        }

        val article = articleService.create(
            ArticleDraft(
                columnId = column.id,
                title = record.content.title,
                bodyHtml = bodyHtml,
                source = record.content.source,
                articleType = record.target.articleType,
                externalUrl = record.content.externalUrl,
                publishDate = record.content.publishDate,
                pinned = false,
                sortOrder = -record.evidence.sourceOrder,
                coverResourceId = null,
                bodyImageResourceIds = bodyImageIds,
                attachmentResourceIds = attachmentIds,
            ),
        )
        val published = articleService.publish(article.id)
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
        return PartyRecordImportResult(record.source.legacyKey, PartyRecordImportStatus.CREATED, published.id)
    }

    private fun validateRecord(record: PartyMigrationRecord) {
        require(record.target.columnAlias == PARTY_THEME_EDUCATION_ALIAS) { "EU-30 扩展 Importer 只接受主题教育栏目" }
        require(record.source.system.isNotBlank() && record.source.system.length <= 100) { "source system 不合法" }
        require(record.source.legacyKey.isNotBlank() && record.source.legacyKey.length <= 255) { "legacy identity 不合法" }
        require(record.source.typeCode == "zhutijiaoyu") { "主题教育 legacy typeCode 不正确：${record.source.typeCode}" }
        require(record.source.detailPath.length <= 500 && record.source.url.length <= 2000) { "legacy URL/path 超长" }
        require(record.sourceFingerprint.matches(EU30_SHA256)) { "source fingerprint 不合法" }
        require(record.evidence.sourceOrder > 0) { "sourceOrder 必须大于 0" }
        require(record.content.title.isNotBlank() && record.content.title.length <= 200) { "标题不合法" }
        require(record.content.source.length <= 200) { "内容来源超长" }
        record.resources.forEach { resource ->
            require(resource.role in setOf("BODY_IMAGE", "ATTACHMENT")) { "未知迁移资源角色：${resource.role}" }
            require(resource.sha256.matches(EU30_SHA256)) { "资源 SHA-256 不合法：${resource.snapshotPath}" }
            val expectedToken = when (resource.role) {
                "BODY_IMAGE" -> "migration-resource://${resource.sha256}"
                "ATTACHMENT" -> "migration-attachment://${resource.sha256}"
                else -> error("unreachable")
            }
            require(resource.token == expectedToken) { "迁移资源 token 与角色/SHA-256 不一致" }
            require(resource.sizeBytes >= 0) { "资源大小不能为负数" }
        }
        when (record.target.articleType) {
            ArticleType.INTERNAL -> require(record.content.externalUrl == null) { "INTERNAL 不能携带 externalUrl" }
            ArticleType.EXTERNAL_LINK -> {
                require(record.content.bodyHtml.isEmpty() && record.resources.isEmpty()) { "EXTERNAL_LINK 不应包含正文或资源" }
                val value = record.content.externalUrl
                require(value != null && value.length <= 2000) { "EXTERNAL_LINK URL 不能为空或超长" }
                val uri = runCatching { URI(value) }.getOrNull()
                require(uri != null && uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank()) { "EXTERNAL_LINK URL 不合法" }
            }
        }
    }

    private fun verifiedSnapshotFile(snapshotRoot: Path, resource: PartyMigrationResource): Path {
        val root = snapshotRoot.toAbsolutePath().normalize()
        val file = root.resolve(resource.snapshotPath).normalize()
        require(file.startsWith(root) && Files.isRegularFile(file)) { "Snapshot 资源不存在或路径越界：${resource.snapshotPath}" }
        require(Files.size(file) == resource.sizeBytes) { "Snapshot 资源大小不一致：${resource.snapshotPath}" }
        val actualHash = Files.newInputStream(file).use { input ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
        require(actualHash == resource.sha256) { "Snapshot 资源 SHA-256 不一致：${resource.snapshotPath}" }
        return file
    }

    private fun sourceFilename(resource: PartyMigrationResource): String {
        val raw = runCatching { Path.of(URI(resource.sourceUrl).path).fileName?.toString() }.getOrNull().orEmpty()
        return raw.ifBlank { "${resource.sha256}.bin" }.takeLast(255)
    }
}

@Service
class PartyHistoricalContentMigrationV2Service(
    private val objectMapper: ObjectMapper,
    private val originalImporter: PartyMigrationRecordImporter,
    private val themeImporter: PartyThemeEducationRecordImporter,
    private val legacyService: PartyHistoricalContentMigrationService,
) {
    fun importSnapshot(snapshotRoot: Path): PartySnapshotImportReport {
        val canonicalIndex = snapshotRoot.resolve("index.ndjson")
        if (!Files.isRegularFile(canonicalIndex)) return legacyService.importSnapshot(snapshotRoot)

        val root = snapshotRoot.toAbsolutePath().normalize()
        val results = mutableListOf<PartyRecordImportResult>()
        Files.newBufferedReader(canonicalIndex).useLines { lines ->
            lines.filter { it.isNotBlank() }.forEachIndexed { index, line ->
                val result = runCatching {
                    val entry = objectMapper.readValue(line, PartyCanonicalIndexEntry::class.java)
                    val articleFile = root.resolve(entry.path).normalize()
                    require(articleFile.startsWith(root) && Files.isRegularFile(articleFile)) { "Canonical article 不存在或路径越界：${entry.path}" }
                    val record = Files.newBufferedReader(articleFile).use { reader -> objectMapper.readValue(reader, PartyMigrationRecord::class.java) }
                    require(record.source.legacyKey == entry.legacyKey) { "Canonical index legacyKey 与 article.json 不一致：${entry.legacyKey}" }
                    if (record.target.columnAlias == PARTY_THEME_EDUCATION_ALIAS) {
                        themeImporter.importRecord(articleFile.parent, record)
                    } else {
                        originalImporter.importRecord(articleFile.parent, record)
                    }
                }.getOrElse { error ->
                    PartyRecordImportResult("index:${index + 1}", PartyRecordImportStatus.INVALID, message = error.message ?: error::class.java.simpleName)
                }
                results += result
            }
        }
        return PartySnapshotImportReport(
            total = results.size,
            created = results.count { it.status == PartyRecordImportStatus.CREATED },
            skipped = results.count { it.status == PartyRecordImportStatus.SKIPPED },
            conflicts = results.count { it.status == PartyRecordImportStatus.CONFLICT },
            invalid = results.count { it.status == PartyRecordImportStatus.INVALID },
            results = results,
        )
    }
}

private class ThemeSnapshotMultipartFile(
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

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "用法：importPartyHistoricalContent <snapshot-root> [Spring Boot args...]" }
    val snapshotRoot = Path.of(args.first()).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(CmsApplication::class.java).web(WebApplicationType.NONE).run(*args.drop(1).toTypedArray())
    try {
        val report = context.getBean(PartyHistoricalContentMigrationV2Service::class.java).importSnapshot(snapshotRoot)
        println("EU29_IMPORT_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
        require(report.conflicts == 0 && report.invalid == 0) { "Party historical import 存在 conflict/invalid，拒绝静默完成" }
    } finally {
        context.close()
    }
}