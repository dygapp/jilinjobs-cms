package com.jilinjobs.cms.migration.generic

import com.jilinjobs.cms.content.ArticleRepository
import com.jilinjobs.cms.content.ArticleType
import com.jilinjobs.cms.listing.CmsListItemRecord
import com.jilinjobs.cms.listing.CmsListItemSourceType
import com.jilinjobs.cms.listing.CmsListMapper
import com.jilinjobs.cms.migration.ArticleLegacyMappingMapper
import com.jilinjobs.cms.migration.CmsListItemLegacyMappingMapper
import com.jilinjobs.cms.migration.CmsListItemLegacyMappingRecord
import com.jilinjobs.cms.resource.ResourceService
import com.jilinjobs.cms.staticresource.StaticResourceNotFoundException
import com.jilinjobs.cms.staticresource.StaticResourceService
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.ObjectMapper

private val COMPATIBILITY_SHA256 = Regex("[0-9a-f]{64}")

data class CanonicalCompatibilityAuthority(
    val version: Int,
    val listItemTransitions: List<CanonicalListItemCompatibilityTransition> = emptyList(),
)

data class CanonicalListItemCompatibilityTransition(
    val listCode: String,
    val sourceSystem: String,
    val legacyKey: String,
    val fromFingerprint: String,
    val fromSourceType: CmsListItemSourceType,
    val fromUrl: String? = null,
    val fromImagePath: String? = null,
    val preserveRuntimeId: Boolean = true,
)

data class GenericCompatibilityOutcome(
    val updated: Map<String, Long>,
    val conflicts: List<GenericMigrationResult>,
)

@Service
class CanonicalCompatibilityLoader(
    private val objectMapper: ObjectMapper,
) {
    fun load(snapshotRoot: Path): CanonicalCompatibilityAuthority? {
        val root = snapshotRoot.toAbsolutePath().normalize()
        val file = root.resolve("compatibility.json")
        if (!Files.isRegularFile(file)) return null
        val authority = Files.newBufferedReader(file).use { reader ->
            runCatching { objectMapper.readValue(reader, CanonicalCompatibilityAuthority::class.java) }
                .getOrElse { throw IllegalArgumentException("Canonical compatibility 无法解析：" + it.message) }
        }
        require(authority.version == 1) { "不支持的 compatibility version：" + authority.version }
        val identities = mutableSetOf<String>()
        authority.listItemTransitions.forEach { transition ->
            require(transition.listCode.isNotBlank()) { "Compatibility listCode 不能为空" }
            require(transition.sourceSystem.isNotBlank()) { "Compatibility sourceSystem 不能为空" }
            require(transition.legacyKey.isNotBlank()) { "Compatibility legacyKey 不能为空" }
            require(transition.fromFingerprint.matches(COMPATIBILITY_SHA256)) { "Compatibility fromFingerprint 不合法" }
            require(transition.preserveRuntimeId) { "Compatibility transition 必须 preserveRuntimeId" }
            transition.fromUrl?.let { validateCompatibilityHttpUrl(it, "Compatibility fromUrl") }
            transition.fromImagePath?.let(CanonicalFileVerifier::validateStaticTarget)
            val key = listOf(
                transition.listCode,
                transition.sourceSystem,
                transition.legacyKey,
                transition.fromFingerprint,
            ).joinToString("\u0000")
            require(identities.add(key)) { "Compatibility transition 重复：" + transition.legacyKey }
        }
        return authority
    }
}

@Service
class GenericListItemCompatibilityService(
    private val mappingMapper: CmsListItemLegacyMappingMapper,
    private val articleMapping: ArticleLegacyMappingMapper,
    private val listMapper: CmsListMapper,
    private val staticResourceService: StaticResourceService,
    private val resourceService: ResourceService,
    private val articleRepository: ArticleRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun apply(
        dataset: LoadedDataset,
        authority: CanonicalCompatibilityAuthority?,
    ): GenericCompatibilityOutcome {
        if (authority == null || authority.listItemTransitions.isEmpty()) {
            return GenericCompatibilityOutcome(emptyMap(), emptyList())
        }
        val updated = linkedMapOf<String, Long>()
        val conflicts = mutableListOf<GenericMigrationResult>()
        dataset.listItems.forEach { loaded ->
            val record = loaded.record
            val existing = mappingMapper.find(loaded.sourceSystem, record.legacyKey) ?: return@forEach
            if (existing.sourceFingerprint == record.sourceFingerprint) return@forEach
            val transition = authority.listItemTransitions.singleOrNull {
                it.listCode == loaded.listCode &&
                    it.sourceSystem == loaded.sourceSystem &&
                    it.legacyKey == record.legacyKey &&
                    it.fromFingerprint == existing.sourceFingerprint
            } ?: return@forEach

            val prepared = runCatching { verifyAndBuildUpdate(loaded, transition, existing) }
                .getOrElse { error ->
                    conflicts += GenericMigrationResult(
                        GenericMigrationKind.LIST_ITEM,
                        loaded.sourceSystem,
                        record.legacyKey,
                        GenericMigrationStatus.CONFLICT,
                        existing.listItemId,
                        error.message ?: error::class.java.simpleName,
                    )
                    return@forEach
                }
            require(listMapper.updateItem(prepared.runtime) == 1) {
                "Compatibility Runtime 原位更新失败：" + record.legacyKey
            }
            require(mappingMapper.update(prepared.mapping) == 1) {
                "Compatibility mapping 更新失败：" + record.legacyKey
            }
            updated[listOf(loaded.sourceSystem, record.legacyKey).joinToString("\u0000")] = existing.listItemId
        }
        return GenericCompatibilityOutcome(updated, conflicts)
    }

    private fun verifyAndBuildUpdate(
        loaded: LoadedListItem,
        transition: CanonicalListItemCompatibilityTransition,
        existing: CmsListItemLegacyMappingRecord,
    ): PreparedCompatibilityUpdate {
        val record = loaded.record
        require(existing.sourceFingerprint == transition.fromFingerprint) { "Compatibility mapping fromFingerprint 已漂移" }
        val list = listMapper.findByCode(loaded.listCode) ?: error("Compatibility target List 不存在：" + loaded.listCode)
        require(list.enabled) { "Compatibility target List 已停用：" + loaded.listCode }
        val current = listMapper.findItem(existing.listItemId)
            ?: error("Compatibility Runtime item 不存在：" + existing.listItemId)

        require(current.id == existing.listItemId && current.listId == list.id) { "Compatibility Runtime list identity 已漂移" }
        require(current.sourceType == transition.fromSourceType.name) { "Compatibility Runtime source type 已漂移" }
        require(current.title == record.title && current.subtitle == record.subtitle) { "Compatibility Runtime title/subtitle 已漂移" }
        require(current.sortOrder == record.sourceOrder && current.enabled == record.enabled) { "Compatibility Runtime order/enabled 已漂移" }

        val expectedOldOpenMode = canonicalOpenModeToRuntime(record.openMode, transition.fromUrl)
        require(current.openMode == expectedOldOpenMode) { "Compatibility Runtime openMode 已漂移" }

        when (transition.fromSourceType) {
            CmsListItemSourceType.LINK -> {
                require(current.articleId == null) { "Compatibility old LINK 不应绑定 Article" }
                require(current.url == transition.fromUrl) { "Compatibility Runtime URL 已漂移" }
            }
            CmsListItemSourceType.ARTICLE -> {
                require(current.articleId != null && current.url == null) { "Compatibility old ARTICLE Runtime 已漂移" }
            }
        }

        val image = loaded.image
        if (transition.fromImagePath != null) {
            require(image != null) { "Compatibility old image 声明存在但 current canonical image 缺失" }
            require(current.imagePath == "/static/" + transition.fromImagePath && current.imageResourceId == null) {
                "Compatibility Runtime image projection 已漂移"
            }
            val oldStatic = try {
                staticResourceService.resolvePublic(transition.fromImagePath)
            } catch (_: StaticResourceNotFoundException) {
                null
            }
            require(oldStatic != null && Files.isRegularFile(oldStatic)) { "Compatibility old static image 不存在" }
            require(Files.size(oldStatic) == image.canonical.sizeBytes) { "Compatibility old static image size 已漂移" }
            require(sha256(oldStatic) == image.canonical.sha256) { "Compatibility old static image SHA-256 已漂移" }
        }

        val oldSourceUrl = transition.fromUrl ?: loaded.sourcePage.orEmpty()
        require(existing.sourceUrl == oldSourceUrl) { "Compatibility mapping source provenance 已漂移" }
        if (image != null) {
            require(existing.imageSourceUrl == image.canonical.sourceUrl && existing.imageSha256 == image.canonical.sha256) {
                "Compatibility mapping image evidence 已漂移"
            }
        }

        val target = buildTargetRuntime(loaded, existing.listItemId, requireNotNull(list.id))
        val mapping = existing.copy(
            sourceUrl = loaded.sourceProvenanceUrl ?: record.url ?: loaded.sourcePage.orEmpty(),
            sourceFingerprint = record.sourceFingerprint,
            imageSourceUrl = image?.canonical?.sourceUrl.orEmpty(),
            imageSha256 = image?.canonical?.sha256.orEmpty(),
        )
        return PreparedCompatibilityUpdate(target, mapping)
    }

    private fun buildTargetRuntime(
        loaded: LoadedListItem,
        runtimeId: Long,
        listId: Long,
    ): CmsListItemRecord {
        val record = loaded.record
        val image = loaded.image
        var imagePath: String? = null
        var imageResourceId: Long? = null
        if (image != null) {
            when (record.sourceType) {
                CmsListItemSourceType.LINK -> {
                    val extension = image.canonical.snapshotPath.substringAfterLast('.').lowercase()
                    val target = record.staticTarget
                        ?: "migrated/content/lists/" + loaded.listCode.uppercase() + "/" + image.canonical.sha256 + "." + extension
                    val safeTarget = CanonicalFileVerifier.validateStaticTarget(target)
                    val existing = try {
                        staticResourceService.resolvePublic(safeTarget)
                    } catch (_: StaticResourceNotFoundException) {
                        null
                    }
                    if (existing == null) {
                        staticResourceService.upload(
                            safeTarget,
                            CompatibilityPathMultipartFile(
                                sourceFilename(image.canonical.sourceUrl, image.canonical.sha256, image.canonical.snapshotPath),
                                image.canonical.contentType,
                                image.file,
                            ),
                            false,
                        )
                    } else {
                        require(sha256(existing) == image.canonical.sha256) { "Compatibility target static image bytes 不一致" }
                    }
                    imagePath = "/static/" + safeTarget
                }
                CmsListItemSourceType.ARTICLE -> {
                    imageResourceId = resourceService.upload(
                        CompatibilityPathMultipartFile(
                            sourceFilename(image.canonical.sourceUrl, image.canonical.sha256, image.canonical.snapshotPath),
                            image.canonical.contentType,
                            image.file,
                        ),
                    ).id
                }
            }
        }

        val articleId = if (record.sourceType == CmsListItemSourceType.ARTICLE) {
            val reference = requireNotNull(record.articleReference) { "Compatibility target ARTICLE 缺少 stable reference" }
            articleMapping.find(reference.sourceSystem, reference.legacyKey)?.articleId
                ?: error("Compatibility target Article mapping 不存在：" + reference.sourceSystem + "/" + reference.legacyKey)
        } else null
        val targetUrl = when (record.sourceType) {
            CmsListItemSourceType.LINK -> record.url
            CmsListItemSourceType.ARTICLE -> articleId
                ?.let(articleRepository::findById)
                ?.takeIf { it.articleType == ArticleType.EXTERNAL_LINK }
                ?.externalUrl
        }
        val runtimeOpenMode = canonicalOpenModeToRuntime(record.openMode, targetUrl)
        val extraJson = objectMapper.writeValueAsString(
            linkedMapOf(
                "migrationSourceSystem" to loaded.sourceSystem,
                "migrationSourcePage" to loaded.sourcePage,
                "legacyKey" to record.legacyKey,
                "sourceFingerprint" to record.sourceFingerprint,
                "imageSourceUrl" to image?.canonical?.sourceUrl,
                "imageSha256" to image?.canonical?.sha256,
                "sourceType" to record.sourceType.name,
                "articleLegacyKey" to record.articleReference?.legacyKey,
            ),
        )
        return CmsListItemRecord(
            id = runtimeId,
            listId = listId,
            sourceType = record.sourceType.name,
            articleId = articleId,
            title = record.title,
            subtitle = record.subtitle,
            url = record.url,
            imagePath = imagePath,
            imageResourceId = imageResourceId,
            openMode = runtimeOpenMode,
            sortOrder = record.sourceOrder,
            enabled = record.enabled,
            extraJson = extraJson,
        )
    }
}

private data class PreparedCompatibilityUpdate(
    val runtime: CmsListItemRecord,
    val mapping: CmsListItemLegacyMappingRecord,
)

private fun validateCompatibilityHttpUrl(value: String, label: String) {
    val uri = runCatching { URI(value) }.getOrNull()
    require(uri != null && uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank()) {
        "$label 不合法"
    }
}

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

private fun sourceFilename(sourceUrl: String, sha: String, snapshotPath: String): String {
    val fromUrl = runCatching { Path.of(URI(sourceUrl).path).fileName?.toString() }.getOrNull().orEmpty()
    val fromSnapshot = runCatching { Path.of(snapshotPath).fileName?.toString() }.getOrNull().orEmpty()
    return fromUrl.ifBlank { fromSnapshot }.ifBlank { "$sha.bin" }.takeLast(255)
}

private class CompatibilityPathMultipartFile(
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
