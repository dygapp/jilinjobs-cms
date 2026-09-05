package com.jilinjobs.cms.migration

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.content.ArticleDraft
import com.jilinjobs.cms.content.ArticleService
import com.jilinjobs.cms.content.ArticleType
import com.jilinjobs.cms.resource.ResourceService
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
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
import java.time.LocalDate

private val PARTY_ALIASES = setOf("party-voice", "party-work", "party-rules", "party-study")
private val SHA256 = Regex("[0-9a-f]{64}")
private val MIGRATION_TOKEN = Regex("migration-(resource|attachment)://[0-9a-f]{64}")
private val CANONICAL_ASSET_REFERENCE = Regex("""(?i)(?:src|href)=[\"']assets/[^\"']+[\"']""")

@Mapper
interface ArticleLegacyMappingMapper {
    @Select(
        """
        SELECT id, source_system, legacy_key, content_id, type_code, detail_path,
               source_url, source_fingerprint, article_id
        FROM cms_article_legacy_mapping
        WHERE source_system=#{sourceSystem} AND legacy_key=#{legacyKey}
        """,
    )
    fun find(
        @Param("sourceSystem") sourceSystem: String,
        @Param("legacyKey") legacyKey: String,
    ): ArticleLegacyMappingRecord?

    @Insert(
        """
        INSERT INTO cms_article_legacy_mapping(
            source_system, legacy_key, content_id, type_code, detail_path,
            source_url, source_fingerprint, article_id
        ) VALUES(
            #{sourceSystem}, #{legacyKey}, #{contentId}, #{typeCode}, #{detailPath},
            #{sourceUrl}, #{sourceFingerprint}, #{articleId}
        )
        """,
    )
    fun insert(record: ArticleLegacyMappingRecord): Int
}

data class ArticleLegacyMappingRecord(
    var id: Long? = null,
    var sourceSystem: String = "",
    var legacyKey: String = "",
    var contentId: String? = null,
    var typeCode: String = "",
    var detailPath: String = "",
    var sourceUrl: String = "",
    var sourceFingerprint: String = "",
    var articleId: Long = 0,
)

data class PartyMigrationSource(
    val system: String,
    val legacyKey: String,
    val contentId: String?,
    val typeCode: String,
    val detailPath: String,
    val url: String,
)

data class PartyMigrationTarget(val columnAlias: String, val articleType: ArticleType)

data class PartyMigrationContent(
    val title: String,
    val source: String,
    val publishDate: LocalDate?,
    val bodyHtml: String,
    val externalUrl: String?,
)

data class PartyMigrationResource(
    val role: String,
    val sourceUrl: String,
    val originalReference: String,
    val token: String,
    val snapshotPath: String,
    val sha256: String,
    val contentType: String?,
    val sizeBytes: Long,
)

data class PartyMigrationEvidence(
    val listPage: Int,
    val listTitle: String,
    val listPublishDate: LocalDate?,
    val sourceOrder: Int,
    val detailPublishDate: LocalDate? = null,
    val rawDetailPath: String? = null,
)

data class PartyMigrationRecord(
    val source: PartyMigrationSource,
    val target: PartyMigrationTarget,
    val content: PartyMigrationContent,
    val resources: List<PartyMigrationResource>,
    val sourceFingerprint: String,
    val evidence: PartyMigrationEvidence,
)

data class PartyCanonicalIndexEntry(
    val legacyKey: String,
    val path: String,
)

enum class PartyRecordImportStatus { CREATED, SKIPPED, CONFLICT, INVALID }

data class PartyRecordImportResult(
    val legacyKey: String,
    val status: PartyRecordImportStatus,
    val articleId: Long? = null,
    val message: String? = null,
)

data class PartySnapshotImportReport(
    val total: Int,
    val created: Int,
    val skipped: Int,
    val conflicts: Int,
    val invalid: Int,
    val results: List<PartyRecordImportResult>,
)

@Service
class PartyMigrationRecordImporter(
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

        // 在任何 Runtime 写入前先完成本条记录的全部 Snapshot 字节校验，避免“前几个文件已上传、后一个资源才发现损坏”的半写入状态。
        val verifiedResources = record.resources.map { resource -> resource to verifiedSnapshotFile(snapshotRoot, resource) }
        var bodyHtml = record.content.bodyHtml
        val bodyImageIds = mutableListOf<Long>()
        val attachmentIds = mutableListOf<Long>()
        for ((resource, file) in verifiedResources) {
            val uploaded = resourceService.upload(SnapshotMultipartFile(sourceFilename(resource), resource.contentType, file))
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
        if (MIGRATION_TOKEN.containsMatchIn(bodyHtml) || CANONICAL_ASSET_REFERENCE.containsMatchIn(bodyHtml)) {
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
                // 原站列表顺序是迁移时的权威排序证据；负值确保后续正常发布的 sortOrder=0 内容自然排在历史内容之前。
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
        require(record.target.columnAlias in PARTY_ALIASES) { "EU-29 Importer 不接受非 Party 栏目：${record.target.columnAlias}" }
        require(record.source.system.isNotBlank() && record.source.system.length <= 100) { "source system 不合法" }
        require(record.source.legacyKey.isNotBlank() && record.source.legacyKey.length <= 255) { "legacy identity 不合法" }
        require(record.source.typeCode.isNotBlank() && record.source.typeCode.length <= 100) { "typeCode 不合法" }
        require(record.source.detailPath.length <= 500 && record.source.url.length <= 2000) { "legacy URL/path 超长" }
        require(record.sourceFingerprint.matches(SHA256)) { "source fingerprint 不合法" }
        require(record.evidence.sourceOrder > 0) { "sourceOrder 必须大于 0" }
        require(record.content.title.isNotBlank() && record.content.title.length <= 200) { "标题不合法" }
        require(record.content.source.length <= 200) { "内容来源超长" }
        record.resources.forEach { resource ->
            require(resource.role in setOf("BODY_IMAGE", "ATTACHMENT")) { "未知迁移资源角色：${resource.role}" }
            require(resource.token.matches(Regex("migration-(resource|attachment)://${resource.sha256}"))) { "迁移资源 token 与 SHA-256 不一致" }
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
        require(resource.sha256.matches(SHA256)) { "资源 SHA-256 不合法：${resource.snapshotPath}" }
        val root = snapshotRoot.toAbsolutePath().normalize()
        val file = root.resolve(resource.snapshotPath).normalize()
        require(file.startsWith(root) && Files.isRegularFile(file)) { "Snapshot 资源不存在或路径越界：${resource.snapshotPath}" }
        val actualSize = Files.size(file)
        require(actualSize == resource.sizeBytes) { "Snapshot 资源大小不一致：${resource.snapshotPath}" }
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
class PartyHistoricalContentMigrationService(
    private val objectMapper: ObjectMapper,
    private val recordImporter: PartyMigrationRecordImporter,
) {
    fun importSnapshot(snapshotRoot: Path): PartySnapshotImportReport {
        val canonicalIndex = snapshotRoot.resolve("index.ndjson")
        val results = if (Files.isRegularFile(canonicalIndex)) {
            importCanonical(snapshotRoot, canonicalIndex)
        } else {
            importLegacy(snapshotRoot)
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

    private fun importCanonical(snapshotRoot: Path, indexFile: Path): List<PartyRecordImportResult> {
        val root = snapshotRoot.toAbsolutePath().normalize()
        val results = mutableListOf<PartyRecordImportResult>()
        Files.newBufferedReader(indexFile).useLines { lines ->
            lines.filter { it.isNotBlank() }.forEachIndexed { index, line ->
                val result = runCatching {
                    val entry = objectMapper.readValue(line, PartyCanonicalIndexEntry::class.java)
                    val articleFile = root.resolve(entry.path).normalize()
                    require(articleFile.startsWith(root) && Files.isRegularFile(articleFile)) { "Canonical article 不存在或路径越界：${entry.path}" }
                    val record = Files.newBufferedReader(articleFile).use { reader -> objectMapper.readValue(reader, PartyMigrationRecord::class.java) }
                    require(record.source.legacyKey == entry.legacyKey) { "Canonical index legacyKey 与 article.json 不一致：${entry.legacyKey}" }
                    recordImporter.importRecord(articleFile.parent, record)
                }.getOrElse { error ->
                    PartyRecordImportResult("index:${index + 1}", PartyRecordImportStatus.INVALID, message = error.message ?: error::class.java.simpleName)
                }
                results += result
            }
        }
        return results
    }

    private fun importLegacy(snapshotRoot: Path): List<PartyRecordImportResult> {
        val file = snapshotRoot.resolve("articles.ndjson")
        require(Files.isRegularFile(file)) { "缺少 Canonical index.ndjson 或 legacy articles.ndjson：$snapshotRoot" }
        val results = mutableListOf<PartyRecordImportResult>()
        Files.newBufferedReader(file).useLines { lines ->
            lines.filter { it.isNotBlank() }.forEachIndexed { index, line ->
                val result = runCatching {
                    val record = objectMapper.readValue(line, PartyMigrationRecord::class.java)
                    recordImporter.importRecord(snapshotRoot, record)
                }.getOrElse { error ->
                    PartyRecordImportResult("line:${index + 1}", PartyRecordImportStatus.INVALID, message = error.message ?: error::class.java.simpleName)
                }
                results += result
            }
        }
        return results
    }
}

private class SnapshotMultipartFile(
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
        val report = context.getBean(PartyHistoricalContentMigrationService::class.java).importSnapshot(snapshotRoot)
        println("EU29_IMPORT_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
        require(report.conflicts == 0 && report.invalid == 0) { "EU-29 import 存在 conflict/invalid，拒绝静默完成" }
    } finally {
        context.close()
    }
}
