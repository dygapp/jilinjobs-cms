package com.jilinjobs.cms.migration.generic

import com.jilinjobs.cms.migration.PageLegacyMappingMapper
import com.jilinjobs.cms.migration.PageLegacyMappingRecord
import com.jilinjobs.cms.page.PageContentDraft
import com.jilinjobs.cms.page.PageMapper
import com.jilinjobs.cms.page.PageRecord
import com.jilinjobs.cms.page.PageRenderMode
import com.jilinjobs.cms.page.PageService
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
import java.util.zip.ZipInputStream
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.ObjectMapper

private val PAGE_SHA256 = Regex("[0-9a-f]{64}")
private val PAGE_ALIAS = Regex("[a-z0-9][a-z0-9-]{0,99}")
private val PAGE_MIGRATION_TOKEN = Regex("migration-resource://[0-9a-f]{64}")
private val PAGE_CANONICAL_ASSET_REFERENCE = Regex("""(?i)(?:src|href)=[\"'](assets/[^\"']+)[\"']""")
private val PAGE_STATIC_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp", "ico", "pdf", "doc", "docx", "xls", "xlsx")

data class CanonicalPageReference(
    val legacyKey: String,
    val path: String,
    val sourceOrder: Int,
    val sourceFingerprint: String,
)

data class CanonicalPageIndex(
    val sourceSystem: String,
    val items: List<CanonicalPageReference>,
)

data class CanonicalPageSource(
    val system: String,
    val legacyKey: String,
    val url: String,
)

data class CanonicalPageTarget(
    val groupAlias: String? = null,
    val pageAlias: String,
)

data class CanonicalPageContent(
    val bodyHtml: String,
    val renderMode: PageRenderMode,
    val embedUrl: String? = null,
)

data class CanonicalPageResource(
    val sourceUrl: String,
    val snapshotPath: String,
    val sha256: String,
    val contentType: String? = null,
    val sizeBytes: Long,
    val token: String,
)

data class CanonicalPageRecord(
    val source: CanonicalPageSource,
    val target: CanonicalPageTarget,
    val content: CanonicalPageContent,
    val resources: List<CanonicalPageResource> = emptyList(),
    val sourceFingerprint: String,
    val expectedTargetFingerprint: String,
)

data class LoadedPageResource(
    val canonical: CanonicalPageResource,
    val file: Path,
)

data class LoadedPage(
    val record: CanonicalPageRecord,
    val sourceOrder: Int,
    val resources: List<LoadedPageResource>,
)

data class PagePlan(
    val loaded: LoadedPage,
    val pageId: Long,
    val action: PlanAction,
    val existingPageId: Long? = null,
)

data class PagePreflightResult(
    val plans: List<PagePlan>,
    val failures: List<GenericMigrationResult>,
)

object GenericPageCanonicalSupport {
    fun load(root: Path, objectMapper: ObjectMapper): List<LoadedPage> {
        val pagesRoot = root.resolve("pages")
        if (!Files.isDirectory(pagesRoot)) return emptyList()
        val indexFile = pagesRoot.resolve("index.json")
        require(Files.isRegularFile(indexFile)) { "Page canonical directory 缺少 index.json" }
        val index = Files.newBufferedReader(indexFile).use { reader ->
            runCatching { objectMapper.readValue(reader, CanonicalPageIndex::class.java) }
                .getOrElse { throw IllegalArgumentException("Page index 无法解析：${it.message}") }
        }
        require(index.sourceSystem.isNotBlank() && index.sourceSystem.length <= 100) { "Page index sourceSystem 不合法" }
        val keys = index.items.map { it.legacyKey }
        require(keys.size == keys.toSet().size) { "Page index 存在重复 legacy identity" }
        val paths = index.items.map { it.path.replace('\\', '/') }
        require(paths.size == paths.toSet().size) { "Page index 存在重复 item path" }

        val pages = index.items.map { reference ->
            require(reference.sourceOrder > 0) { "Page sourceOrder 必须大于 0：${reference.legacyKey}" }
            require(reference.sourceFingerprint.matches(PAGE_SHA256)) { "Page index fingerprint 不合法：${reference.legacyKey}" }
            val itemFile = CanonicalFileVerifier.resolveRegularFile(pagesRoot, reference.path, "Page item path")
            val record = Files.newBufferedReader(itemFile).use { reader ->
                runCatching { objectMapper.readValue(reader, CanonicalPageRecord::class.java) }
                    .getOrElse { throw IllegalArgumentException("Canonical Page 无法解析：${reference.path}: ${it.message}") }
            }
            require(record.source.system == index.sourceSystem) { "Page index sourceSystem 与 page.json 不一致：${reference.legacyKey}" }
            require(record.source.legacyKey == reference.legacyKey) { "Page index legacyKey 与 page.json 不一致：${reference.legacyKey}" }
            require(record.sourceFingerprint == reference.sourceFingerprint) { "Page index fingerprint 与 page.json 不一致：${reference.legacyKey}" }
            val resources = record.resources.map { resource ->
                val file = CanonicalFileVerifier.verifyFile(itemFile.parent, resource.snapshotPath, resource.sizeBytes, resource.sha256, "Page resource")
                validateStaticContent(file, resource.snapshotPath)
                LoadedPageResource(resource, file)
            }
            LoadedPage(record, reference.sourceOrder, resources)
        }.sortedWith(compareBy<LoadedPage> { it.sourceOrder }.thenBy { pageIdentity(it.record.source.system, it.record.source.legacyKey) })
        validate(pages)
        return pages
    }

    fun validate(pages: List<LoadedPage>) {
        val identities = pages.map { pageIdentity(it.record.source.system, it.record.source.legacyKey) }
        require(identities.size == identities.toSet().size) { "Canonical Page dataset 存在重复 stable identity" }
        pages.forEach(::validatePage)
    }

    private fun validatePage(loaded: LoadedPage) {
        val record = loaded.record
        require(record.source.system.isNotBlank() && record.source.system.length <= 100) { "Page source system 不合法" }
        require(record.source.legacyKey.isNotBlank() && record.source.legacyKey.length <= 255) { "Page legacy identity 不合法" }
        validateHttpUrl(record.source.url, "Page source URL")
        require(record.sourceFingerprint.matches(PAGE_SHA256)) { "Page source fingerprint 不合法：${record.source.legacyKey}" }
        require(record.expectedTargetFingerprint.matches(PAGE_SHA256)) { "Page expectedTargetFingerprint 不合法：${record.source.legacyKey}" }
        require(record.target.pageAlias.matches(PAGE_ALIAS)) { "Page target alias 不合法：${record.target.pageAlias}" }
        record.target.groupAlias?.let { require(it.matches(PAGE_ALIAS)) { "Page target groupAlias 不合法：$it" } }
        if (record.content.renderMode == PageRenderMode.INTERNAL_STATIC && !record.content.embedUrl.isNullOrBlank()) {
            require(record.content.embedUrl.startsWith("/")) { "Page INTERNAL_STATIC embedUrl 必须使用本站路径" }
        }
        val resourceTokens = record.resources.map { it.token }
        require(resourceTokens.size == resourceTokens.toSet().size) { "Page resource token 重复：${record.source.legacyKey}" }
        val resourcePaths = record.resources.map { it.snapshotPath.replace('\\', '/') }
        require(resourcePaths.size == resourcePaths.toSet().size) { "Page resource snapshotPath 重复：${record.source.legacyKey}" }
        require(loaded.resources.size == record.resources.size) { "Loaded Page resource 与 canonical resources 不一致：${record.source.legacyKey}" }
        loaded.resources.forEach { loadedResource ->
            val resource = loadedResource.canonical
            require(resource.sha256.matches(PAGE_SHA256)) { "Page resource SHA-256 不合法：${resource.snapshotPath}" }
            require(resource.sizeBytes > 0) { "Page resource 不能为空：${resource.snapshotPath}" }
            require(resource.snapshotPath.isNotBlank()) { "Page resource snapshotPath 不能为空" }
            validateHttpUrl(resource.sourceUrl, "Page resource source URL")
            require(resource.token == "migration-resource://${resource.sha256}") { "Page resource token 与 SHA-256 不一致" }
            val extension = resource.snapshotPath.substringAfterLast('.', "").lowercase()
            require(extension in PAGE_STATIC_EXTENSIONS) { "Page resource 扩展名不支持：$extension" }
            require(Files.isRegularFile(loadedResource.file)) { "Loaded Page resource 不存在：${resource.snapshotPath}" }
            require(Files.size(loadedResource.file) == resource.sizeBytes) { "Loaded Page resource size 不一致：${resource.snapshotPath}" }
            require(sha256Path(loadedResource.file) == resource.sha256) { "Loaded Page resource SHA-256 不一致：${resource.snapshotPath}" }
            validateStaticContent(loadedResource.file, resource.snapshotPath)
            CanonicalFileVerifier.validateStaticTarget(staticTarget(resource))
        }
        val declaredTokens = record.resources.map { it.token }.toSet()
        PAGE_MIGRATION_TOKEN.findAll(record.content.bodyHtml).forEach { match ->
            require(match.value in declaredTokens) { "Page body 存在未声明 migration token：${match.value}" }
        }
        val declaredPaths = record.resources.map { it.snapshotPath.replace('\\', '/') }.toSet()
        PAGE_CANONICAL_ASSET_REFERENCE.findAll(record.content.bodyHtml).forEach { match ->
            require(match.groupValues[1] in declaredPaths) { "Page body 存在未声明 canonical asset reference：${match.groupValues[1]}" }
        }
    }
}

@Service
class GenericPagePreflight(
    private val pageMapper: PageMapper,
    private val mappingMapper: PageLegacyMappingMapper,
    private val staticResourceService: StaticResourceService,
    private val objectMapper: ObjectMapper,
) {
    fun preflight(pages: List<LoadedPage>): PagePreflightResult {
        val plans = mutableListOf<PagePlan>()
        val failures = mutableListOf<GenericMigrationResult>()
        pages.forEach { loaded ->
            val record = loaded.record
            val target = runCatching {
                if (record.target.groupAlias == null) {
                    pageMapper.findStandalone(record.target.pageAlias)
                } else {
                    pageMapper.findGrouped(record.target.groupAlias, record.target.pageAlias)
                }
            }.getOrElse { error ->
                failures += invalid(record, "Page stable target 无法唯一解析：${targetLabel(record.target)}: ${error.message}")
                return@forEach
            }
            if (target == null) {
                failures += invalid(record, "Page stable target 不存在：${targetLabel(record.target)}")
                return@forEach
            }
            val pageId = requireNotNull(target.id)
            val existing = mappingMapper.find(record.source.system, record.source.legacyKey)
            if (existing != null) {
                when {
                    existing.pageId != pageId -> failures += conflict(record, existing.pageId, "Page migration mapping 指向不同 stable target")
                    existing.sourceFingerprint != record.sourceFingerprint -> failures += conflict(record, pageId, "Page stable identity 已存在，但 source fingerprint 已变化")
                    else -> plans += PagePlan(loaded, pageId, PlanAction.SKIP, existing.pageId)
                }
                return@forEach
            }
            val runtimeFingerprint = pageContentFingerprint(target, objectMapper)
            if (runtimeFingerprint != record.expectedTargetFingerprint) {
                failures += conflict(record, pageId, "Page target content 与 expectedTargetFingerprint 不一致")
                return@forEach
            }
            loaded.resources.forEach { resource ->
                val staticTarget = staticTarget(resource.canonical)
                val existingFile = try {
                    staticResourceService.resolvePublic(staticTarget)
                } catch (_: StaticResourceNotFoundException) {
                    null
                }
                if (existingFile != null && sha256Path(existingFile) != resource.canonical.sha256) {
                    failures += conflict(record, pageId, "Page static target 已存在但 bytes 不一致：$staticTarget")
                    return@forEach
                }
            }
            plans += PagePlan(loaded, pageId, PlanAction.CREATE)
        }
        return PagePreflightResult(plans, failures)
    }

    private fun invalid(record: CanonicalPageRecord, message: String) =
        GenericMigrationResult(GenericMigrationKind.PAGE, record.source.system, record.source.legacyKey, GenericMigrationStatus.INVALID, message = message)

    private fun conflict(record: CanonicalPageRecord, runtimeId: Long?, message: String) =
        GenericMigrationResult(GenericMigrationKind.PAGE, record.source.system, record.source.legacyKey, GenericMigrationStatus.CONFLICT, runtimeId, message)
}

@Service
class GenericPageImporter(
    private val pageMapper: PageMapper,
    private val pageService: PageService,
    private val mappingMapper: PageLegacyMappingMapper,
    private val staticResourceService: StaticResourceService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(plan: PagePlan): GenericMigrationResult {
        val record = plan.loaded.record
        if (plan.action == PlanAction.SKIP) {
            return GenericMigrationResult(GenericMigrationKind.PAGE, record.source.system, record.source.legacyKey, GenericMigrationStatus.SKIPPED, plan.existingPageId)
        }
        val current = pageMapper.findPageById(plan.pageId) ?: error("Page target 在 execute 前消失：${plan.pageId}")
        require(pageContentFingerprint(current, objectMapper) == record.expectedTargetFingerprint) {
            "Page target content 在 preflight 后发生变化，拒绝覆盖：${targetLabel(record.target)}"
        }

        var bodyHtml = record.content.bodyHtml
        plan.loaded.resources.forEach { loaded ->
            val resource = loaded.canonical
            val target = staticTarget(resource)
            val existing = try {
                staticResourceService.resolvePublic(target)
            } catch (_: StaticResourceNotFoundException) {
                null
            }
            if (existing == null) {
                staticResourceService.upload(
                    target,
                    PagePathMultipartFile(sourceFilename(resource), resource.contentType, loaded.file),
                    false,
                )
            } else {
                require(sha256Path(existing) == resource.sha256) { "Page static target bytes changed after preflight：$target" }
            }
            val runtimePath = "/static/$target"
            bodyHtml = bodyHtml.replace(resource.token, runtimePath).replace(resource.snapshotPath, runtimePath)
        }
        require(!PAGE_MIGRATION_TOKEN.containsMatchIn(bodyHtml) && !PAGE_CANONICAL_ASSET_REFERENCE.containsMatchIn(bodyHtml)) {
            "Page execute 后仍存在未解析 canonical resource reference"
        }
        val updated = pageService.updateContent(
            plan.pageId,
            PageContentDraft(bodyHtml, record.content.renderMode, record.content.embedUrl),
        )
        mappingMapper.insert(
            PageLegacyMappingRecord(
                sourceSystem = record.source.system,
                legacyKey = record.source.legacyKey,
                sourceUrl = record.source.url,
                sourceFingerprint = record.sourceFingerprint,
                pageId = updated.id,
            ),
        )
        return GenericMigrationResult(GenericMigrationKind.PAGE, record.source.system, record.source.legacyKey, GenericMigrationStatus.CREATED, updated.id)
    }
}

fun pageContentFingerprint(
    bodyHtml: String,
    renderMode: PageRenderMode,
    embedUrl: String?,
    objectMapper: ObjectMapper,
): String = sha256Bytes(
    objectMapper.writeValueAsBytes(
        linkedMapOf<String, Any?>(
            "bodyHtml" to bodyHtml,
            "renderMode" to renderMode.name,
            "embedUrl" to embedUrl,
        ),
    ),
)

fun pageContentFingerprint(record: PageRecord, objectMapper: ObjectMapper): String =
    pageContentFingerprint(record.bodyHtml, PageRenderMode.valueOf(record.renderMode), record.embedUrl, objectMapper)

private fun targetLabel(target: CanonicalPageTarget) = "${target.groupAlias ?: "<root>"}/${target.pageAlias}"

private fun pageIdentity(sourceSystem: String, legacyKey: String) = "$sourceSystem\u0000$legacyKey"

private fun staticTarget(resource: CanonicalPageResource): String {
    val extension = resource.snapshotPath.substringAfterLast('.', "").lowercase()
    require(extension in PAGE_STATIC_EXTENSIONS) { "Page resource 扩展名不支持：$extension" }
    return CanonicalFileVerifier.validateStaticTarget("migrated/content/pages/${resource.sha256}.$extension")
}

private fun sourceFilename(resource: CanonicalPageResource): String {
    val fromUrl = runCatching { Path.of(URI(resource.sourceUrl).path).fileName?.toString() }.getOrNull().orEmpty()
    val fromSnapshot = runCatching { Path.of(resource.snapshotPath).fileName?.toString() }.getOrNull().orEmpty()
    return fromUrl.ifBlank { fromSnapshot }.ifBlank { "${resource.sha256}.bin" }.takeLast(255)
}

private fun validateHttpUrl(value: String, label: String) {
    require(value.length <= 2000) { "$label 过长" }
    val uri = runCatching { URI(value) }.getOrNull()
    require(uri != null && uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank()) { "$label 不合法" }
}

private fun validateStaticContent(file: Path, raw: String) {
    val extension = raw.substringAfterLast('.', "").lowercase()
    require(extension in PAGE_STATIC_EXTENSIONS) { "Page resource 扩展名不支持：$extension" }
    val header = Files.newInputStream(file).use { it.readNBytes(16) }
    val matches = when (extension) {
        "png" -> header.startsWith(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a))
        "jpg", "jpeg" -> header.size >= 2 && header[0] == 0xff.toByte() && header[1] == 0xd8.toByte()
        "gif" -> header.asAscii(6) in setOf("GIF87a", "GIF89a")
        "webp" -> header.asAscii(4) == "RIFF" && header.drop(8).take(4).toByteArray().toString(StandardCharsets.US_ASCII) == "WEBP"
        "ico" -> header.startsWith(byteArrayOf(0x00, 0x00, 0x01, 0x00))
        "pdf" -> header.asAscii(5) == "%PDF-"
        "doc", "xls" -> header.startsWith(byteArrayOf(0xd0.toByte(), 0xcf.toByte(), 0x11, 0xe0.toByte(), 0xa1.toByte(), 0xb1.toByte(), 0x1a, 0xe1.toByte()))
        "docx" -> officeOpenXml(file, "word/")
        "xlsx" -> officeOpenXml(file, "xl/")
        else -> false
    }
    require(matches) { "Page resource 实际内容与 .$extension 扩展名不匹配：$raw" }
}

private fun officeOpenXml(file: Path, requiredPrefix: String): Boolean = runCatching {
    ZipInputStream(Files.newInputStream(file)).use { zip ->
        var item = zip.nextEntry
        while (item != null) {
            if (item.name.startsWith(requiredPrefix)) return@use true
            item = zip.nextEntry
        }
        false
    }
}.getOrDefault(false)

private fun ByteArray.startsWith(prefix: ByteArray): Boolean = size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
private fun ByteArray.asAscii(length: Int): String = take(length).toByteArray().toString(StandardCharsets.US_ASCII)

private fun sha256Bytes(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

private fun sha256Path(path: Path): String = Files.newInputStream(path).use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(8192)
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        digest.update(buffer, 0, read)
    }
    digest.digest().joinToString("") { "%02x".format(it) }
}

private class PagePathMultipartFile(
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
