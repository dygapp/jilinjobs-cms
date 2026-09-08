package com.jilinjobs.cms.migration.party

import com.jilinjobs.cms.listing.CmsListItemRecord
import com.jilinjobs.cms.listing.CmsListItemSourceType
import com.jilinjobs.cms.listing.CmsListMapper
import com.jilinjobs.cms.migration.ArticleLegacyMappingMapper
import com.jilinjobs.cms.migration.CmsListItemLegacyMappingMapper
import com.jilinjobs.cms.migration.CmsListItemLegacyMappingRecord
import com.jilinjobs.cms.migration.PartyCanonicalIndexEntry
import com.jilinjobs.cms.migration.PartyCarouselCanonicalIndex
import com.jilinjobs.cms.migration.PartyCarouselPlacementImportReport
import com.jilinjobs.cms.migration.PartyCarouselPlacementImportResult
import com.jilinjobs.cms.migration.PartyCarouselPlacementImportStatus
import com.jilinjobs.cms.migration.PartyCarouselPlacementItem
import com.jilinjobs.cms.migration.PartyMigrationRecord
import com.jilinjobs.cms.migration.PartyRecordImportResult
import com.jilinjobs.cms.migration.PartyRecordImportStatus
import com.jilinjobs.cms.migration.PartySnapshotImportReport
import com.jilinjobs.cms.migration.generic.CanonicalArticleContent
import com.jilinjobs.cms.migration.generic.CanonicalArticleEvidence
import com.jilinjobs.cms.migration.generic.CanonicalArticleRecord
import com.jilinjobs.cms.migration.generic.CanonicalArticleReference
import com.jilinjobs.cms.migration.generic.CanonicalArticleSource
import com.jilinjobs.cms.migration.generic.CanonicalArticleTarget
import com.jilinjobs.cms.migration.generic.CanonicalDatasetValidator
import com.jilinjobs.cms.migration.generic.CanonicalFileVerifier
import com.jilinjobs.cms.migration.generic.CanonicalListImage
import com.jilinjobs.cms.migration.generic.CanonicalListItemRecord
import com.jilinjobs.cms.migration.generic.CanonicalMigrationResource
import com.jilinjobs.cms.migration.generic.GenericContentMigrationReport
import com.jilinjobs.cms.migration.generic.GenericContentMigrationService
import com.jilinjobs.cms.migration.generic.GenericMigrationKind
import com.jilinjobs.cms.migration.generic.GenericMigrationResult
import com.jilinjobs.cms.migration.generic.GenericMigrationStatus
import com.jilinjobs.cms.migration.generic.LoadedArticle
import com.jilinjobs.cms.migration.generic.LoadedDataset
import com.jilinjobs.cms.migration.generic.LoadedListImage
import com.jilinjobs.cms.migration.generic.LoadedListItem
import com.jilinjobs.cms.migration.generic.LoadedResource
import com.jilinjobs.cms.resource.ResourceService
import com.jilinjobs.cms.staticresource.StaticResourceNotFoundException
import com.jilinjobs.cms.staticresource.StaticResourceService
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.ObjectMapper

private val PARTY_SHA256 = Regex("[0-9a-f]{64}")
private const val PARTY_STATIC_PROJECTION_PREFIX = "migrated/party/carousel"

data class PartyContentScope(
    val typeCode: String,
    val columnAlias: String,
    val observedListCount: Int,
)

data class PartyManifest(
    val sourceSystem: String,
    val contentScope: List<PartyContentScope>,
)

data class PartyCompatibilityAuthority(
    val compatibilityVersion: Int,
    val transitions: List<PartyCompatibilityTransition>,
)

data class PartyCompatibilityTransition(
    val kind: String,
    val listCode: String,
    val sourceSystem: String,
    val legacyKey: String,
    val fromFingerprint: String,
    val fromSourceType: String,
    val preserveRuntimeId: Boolean,
)

data class PartyPreparedCarousel(
    val dataset: LoadedDataset,
    val compatibility: PartyCompatibilityAuthority?,
)

data class CompatibilityConflict(
    val runtimeId: Long?,
    val message: String,
)

data class CompatibilityOutcome(
    val updated: Map<String, Long>,
    val conflicts: Map<String, CompatibilityConflict>,
)

@Service
class PartyCanonicalAdapter(
    private val objectMapper: ObjectMapper,
) {
    fun loadArticles(snapshotRoot: Path): LoadedDataset {
        val root = normalizedRoot(snapshotRoot)
        val manifest = loadManifest(root)
        val indexFile = CanonicalFileVerifier.resolveRegularFile(root, "index.ndjson", "Party Article index")
        val scopes = manifest.contentScope.associateBy { it.columnAlias }
        require(scopes.isNotEmpty()) { "Party manifest contentScope 不能为空" }
        val loaded = mutableListOf<LoadedArticle>()
        Files.newBufferedReader(indexFile).useLines { lines ->
            lines.filter(String::isNotBlank).forEachIndexed { index, line ->
                val entry = runCatching { objectMapper.readValue(line, PartyCanonicalIndexEntry::class.java) }
                    .getOrElse { throw IllegalArgumentException("Party Article index 第 ${index + 1} 行无法解析：${it.message}") }
                val articleFile = CanonicalFileVerifier.resolveRegularFile(root, entry.path, "Party Article path")
                val party = Files.newBufferedReader(articleFile).use { reader ->
                    runCatching { objectMapper.readValue(reader, PartyMigrationRecord::class.java) }
                        .getOrElse { throw IllegalArgumentException("Party Article 无法解析：${entry.path}: ${it.message}") }
                }
                require(party.source.legacyKey == entry.legacyKey) { "Party Article index legacyKey 与 article.json 不一致：${entry.legacyKey}" }
                entry.sourceFingerprint?.let { require(it == party.sourceFingerprint) { "Party Article index fingerprint 不一致：${entry.legacyKey}" } }
                entry.columnAlias?.let { require(it == party.target.columnAlias) { "Party Article index columnAlias 不一致：${entry.legacyKey}" } }
                entry.articleType?.let { require(it == party.target.articleType.name) { "Party Article index articleType 不一致：${entry.legacyKey}" } }
                require(party.source.system == manifest.sourceSystem) { "Party Article sourceSystem 与 manifest 不一致：${entry.legacyKey}" }
                val scope = scopes[party.target.columnAlias]
                    ?: throw IllegalArgumentException("Party Article target alias 不属于 manifest contentScope：${party.target.columnAlias}")
                require(scope.typeCode == party.source.typeCode) { "Party Article typeCode 与 manifest contentScope 不一致：${entry.legacyKey}" }

                val canonical = party.toCanonical()
                val resources = canonical.resources.distinctBy { it.token }.map { resource ->
                    LoadedResource(
                        resource,
                        CanonicalFileVerifier.verifyFile(articleFile.parent, resource.snapshotPath, resource.sizeBytes, resource.sha256, "Party Article resource"),
                    )
                }
                loaded += LoadedArticle(canonical, resources)
            }
        }
        val counts = loaded.groupingBy { it.record.target.columnAlias }.eachCount()
        val expected = manifest.contentScope.associate { it.columnAlias to it.observedListCount }
        require(counts == expected) { "Party Article scope count 与 manifest 不一致：actual=$counts expected=$expected" }
        return LoadedDataset(loaded, emptyList()).also(CanonicalDatasetValidator::validate)
    }

    fun loadCarousel(snapshotRoot: Path): PartyPreparedCarousel {
        val root = normalizedRoot(snapshotRoot)
        val manifest = loadManifest(root)
        val listsRoot = root.resolve("lists")
        require(Files.isDirectory(listsRoot)) { "Party canonical dataset 缺少 lists directory" }
        val listRoots = Files.list(listsRoot).use { stream ->
            stream.filter(Files::isDirectory)
                .filter { Files.isRegularFile(it.resolve("index.json")) }
                .sorted()
                .toList()
        }
        require(listRoots.size == 1) { "Phase 2C Party dataset 需要恰好一个 canonical list，实际 ${listRoots.size}" }
        val listRoot = listRoots.single()
        val indexFile = listRoot.resolve("index.json")
        val index = Files.newBufferedReader(indexFile).use { reader ->
            runCatching { objectMapper.readValue(reader, PartyCarouselCanonicalIndex::class.java) }
                .getOrElse { throw IllegalArgumentException("Party List index 无法解析：${it.message}") }
        }
        require(index.sourceSystem == manifest.sourceSystem) { "Party List sourceSystem 与 manifest 不一致" }
        require(listRoot.fileName.toString().equals(index.listCode, ignoreCase = true)) { "Party List directory 与 listCode 不一致" }
        require(index.items.isNotEmpty()) { "Party List canonical index 不能为空" }
        require(index.items.map { it.legacyKey }.distinct().size == index.items.size) { "Party List index legacy identity 重复" }
        require(index.items.map { it.sourceOrder }.distinct().size == index.items.size) { "Party List index sourceOrder 重复" }

        val loaded = index.items.sortedBy { it.sourceOrder }.map { reference ->
            val itemFile = CanonicalFileVerifier.resolveRegularFile(listRoot, reference.path, "Party List item path")
            val party = Files.newBufferedReader(itemFile).use { reader ->
                runCatching { objectMapper.readValue(reader, PartyCarouselPlacementItem::class.java) }
                    .getOrElse { throw IllegalArgumentException("Party List item 无法解析：${reference.path}: ${it.message}") }
            }
            require(party.legacyKey == reference.legacyKey) { "Party List index legacyKey 与 item.json 不一致：${reference.legacyKey}" }
            require(party.sourceOrder == reference.sourceOrder) { "Party List index sourceOrder 与 item.json 不一致：${reference.legacyKey}" }
            require(party.sourceFingerprint == reference.sourceFingerprint) { "Party List index fingerprint 与 item.json 不一致：${reference.legacyKey}" }
            val image = party.image.toCanonical().let { canonical ->
                val file = CanonicalFileVerifier.verifyFile(itemFile.parent, canonical.snapshotPath, canonical.sizeBytes, canonical.sha256, "Party List image")
                CanonicalFileVerifier.validateStaticImageBytes(file, canonical.snapshotPath)
                LoadedListImage(canonical, file)
            }
            val sourceType = party.sourceType
            val articleReference = party.articleRef?.let { CanonicalArticleReference(it.sourceSystem, it.legacyKey) }
            val runtimeUrl = if (sourceType == CmsListItemSourceType.LINK) requireNotNull(party.url) { "Party LINK item URL 不能为空" } else null
            if (sourceType == CmsListItemSourceType.LINK) require(articleReference == null) { "Party LINK item 不允许 articleRef" }
            if (sourceType == CmsListItemSourceType.ARTICLE) require(articleReference != null) { "Party ARTICLE item 必须提供 articleRef" }
            val record = CanonicalListItemRecord(
                legacyKey = party.legacyKey,
                sourceOrder = party.sourceOrder,
                sourceType = sourceType,
                title = party.title,
                url = runtimeUrl,
                articleReference = articleReference,
                openMode = party.openMode,
                enabled = true,
                sourceFingerprint = party.sourceFingerprint,
                image = image.canonical,
            )
            LoadedListItem(
                listCode = index.listCode,
                sourceSystem = index.sourceSystem,
                sourcePage = index.sourcePage,
                record = record,
                image = image,
                sourceProvenanceUrl = party.url ?: index.sourcePage,
                staticTarget = if (sourceType == CmsListItemSourceType.LINK) partyStaticTarget(party.image.snapshotPath, party.image.sha256) else null,
            )
        }
        val dataset = LoadedDataset(emptyList(), loaded).also(CanonicalDatasetValidator::validate)
        return PartyPreparedCarousel(dataset, loadCompatibility(root))
    }

    private fun loadManifest(root: Path): PartyManifest {
        val file = CanonicalFileVerifier.resolveRegularFile(root, "manifest.json", "Party manifest")
        val manifest = Files.newBufferedReader(file).use { reader ->
            runCatching { objectMapper.readValue(reader, PartyManifest::class.java) }
                .getOrElse { throw IllegalArgumentException("Party manifest 无法解析：${it.message}") }
        }
        require(manifest.sourceSystem.isNotBlank()) { "Party manifest sourceSystem 不能为空" }
        require(manifest.contentScope.map { it.columnAlias }.distinct().size == manifest.contentScope.size) { "Party manifest columnAlias 重复" }
        require(manifest.contentScope.all { it.typeCode.isNotBlank() && it.columnAlias.isNotBlank() && it.observedListCount >= 0 }) { "Party manifest contentScope 不合法" }
        return manifest
    }

    private fun loadCompatibility(root: Path): PartyCompatibilityAuthority? {
        val explicit = System.getenv("PARTY_MIGRATION_COMPATIBILITY_FILE")?.trim()?.takeIf(String::isNotEmpty)
        val file = if (explicit != null) {
            val path = Path.of(explicit).toAbsolutePath().normalize()
            require(Files.isRegularFile(path)) { "显式 Party compatibility authority 不存在：$path" }
            path
        } else {
            root.resolve("compatibility.json").takeIf(Files::isRegularFile)
        } ?: return null
        val authority = Files.newBufferedReader(file).use { reader ->
            runCatching { objectMapper.readValue(reader, PartyCompatibilityAuthority::class.java) }
                .getOrElse { throw IllegalArgumentException("Party compatibility authority 无法解析：${it.message}") }
        }
        require(authority.compatibilityVersion == 1) { "不支持的 Party compatibilityVersion：${authority.compatibilityVersion}" }
        val identities = mutableSetOf<String>()
        authority.transitions.forEach { transition ->
            require(transition.kind == "LIST_ITEM") { "Party compatibility transition kind 不支持：${transition.kind}" }
            require(transition.listCode.isNotBlank() && transition.sourceSystem.isNotBlank() && transition.legacyKey.isNotBlank()) { "Party compatibility transition identity 不完整" }
            require(transition.fromFingerprint.matches(PARTY_SHA256)) { "Party compatibility fromFingerprint 不合法" }
            runCatching { CmsListItemSourceType.valueOf(transition.fromSourceType) }
                .getOrElse { throw IllegalArgumentException("Party compatibility fromSourceType 不合法：${transition.fromSourceType}") }
            require(transition.preserveRuntimeId) { "Phase 2C compatibility transition 必须 preserveRuntimeId" }
            val identity = "${transition.listCode}\u0000${transition.sourceSystem}\u0000${transition.legacyKey}\u0000${transition.fromFingerprint}"
            require(identities.add(identity)) { "Party compatibility transition 重复：${transition.legacyKey}" }
        }
        return authority
    }

    private fun normalizedRoot(snapshotRoot: Path): Path {
        val root = snapshotRoot.toAbsolutePath().normalize()
        require(Files.isDirectory(root)) { "Party snapshot root 不存在：$root" }
        return root
    }

    private fun PartyMigrationRecord.toCanonical() = CanonicalArticleRecord(
        source = CanonicalArticleSource(source.system, source.legacyKey, source.contentId, source.typeCode, source.detailPath, source.url),
        target = CanonicalArticleTarget(target.columnAlias, target.articleType),
        content = CanonicalArticleContent(content.title, content.source, content.publishDate, content.bodyHtml, content.externalUrl),
        resources = resources.map {
            CanonicalMigrationResource(it.role, it.sourceUrl, it.originalReference, it.token, it.snapshotPath, it.sha256, it.contentType, it.sizeBytes)
        },
        sourceFingerprint = sourceFingerprint,
        evidence = CanonicalArticleEvidence(evidence.listPage, evidence.listTitle, evidence.listPublishDate, evidence.sourceOrder, evidence.detailPublishDate, evidence.rawDetailPath),
    )

    private fun com.jilinjobs.cms.migration.PartyCarouselSnapshotImage.toCanonical() = CanonicalListImage(
        sourceUrl = sourceUrl,
        snapshotPath = snapshotPath,
        sha256 = sha256,
        contentType = contentType,
        sizeBytes = sizeBytes,
    )

    private fun partyStaticTarget(snapshotPath: String, sha256: String): String {
        val extension = snapshotPath.substringAfterLast('.', "").lowercase()
        require(extension.isNotBlank()) { "Party static projection 缺少图片扩展名" }
        return CanonicalFileVerifier.validateStaticTarget("$PARTY_STATIC_PROJECTION_PREFIX/$sha256.$extension")
    }
}

@Mapper
interface PartyCompatibilityMappingMapper {
    @Update(
        """
        UPDATE cms_list_item_legacy_mapping
        SET source_url=#{sourceUrl}, source_fingerprint=#{sourceFingerprint},
            image_source_url=#{imageSourceUrl}, image_sha256=#{imageSha256}
        WHERE id=#{id}
        """,
    )
    fun update(record: CmsListItemLegacyMappingRecord): Int
}

@Service
class PartyCompatibilityService(
    private val mappingMapper: CmsListItemLegacyMappingMapper,
    private val correctionMapper: PartyCompatibilityMappingMapper,
    private val articleMapping: ArticleLegacyMappingMapper,
    private val listMapper: CmsListMapper,
    private val staticResourceService: StaticResourceService,
    private val resourceService: ResourceService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun apply(dataset: LoadedDataset, authority: PartyCompatibilityAuthority?): CompatibilityOutcome {
        if (authority == null) return CompatibilityOutcome(emptyMap(), emptyMap())
        val updated = linkedMapOf<String, Long>()
        val conflicts = linkedMapOf<String, CompatibilityConflict>()
        dataset.listItems.forEach { loaded ->
            val record = loaded.record
            val existing = mappingMapper.find(loaded.sourceSystem, record.legacyKey) ?: return@forEach
            if (existing.sourceFingerprint == record.sourceFingerprint) return@forEach
            val transition = authority.transitions.singleOrNull {
                it.listCode == loaded.listCode &&
                    it.sourceSystem == loaded.sourceSystem &&
                    it.legacyKey == record.legacyKey &&
                    it.fromFingerprint == existing.sourceFingerprint
            } ?: return@forEach
            val guard = runCatching { verifyAndBuildUpdate(loaded, transition, existing) }
            val prepared = guard.getOrElse { error ->
                conflicts[record.legacyKey] = CompatibilityConflict(existing.listItemId, error.message ?: error::class.java.simpleName)
                return@forEach
            }
            val changed = listMapper.updateItem(prepared.runtime)
            require(changed == 1) { "Party compatibility Runtime 原位更新失败：${record.legacyKey}" }
            require(correctionMapper.update(prepared.mapping) == 1) { "Party compatibility mapping 更新失败：${record.legacyKey}" }
            updated[record.legacyKey] = existing.listItemId
        }
        return CompatibilityOutcome(updated, conflicts)
    }

    private fun verifyAndBuildUpdate(
        loaded: LoadedListItem,
        transition: PartyCompatibilityTransition,
        existing: CmsListItemLegacyMappingRecord,
    ): PreparedCompatibilityUpdate {
        val record = loaded.record
        require(transition.preserveRuntimeId) { "Compatibility transition 未声明 preserveRuntimeId" }
        require(existing.sourceFingerprint == transition.fromFingerprint) { "Compatibility mapping fromFingerprint 已漂移" }
        val fromSourceType = CmsListItemSourceType.valueOf(transition.fromSourceType)
        require(fromSourceType == CmsListItemSourceType.LINK) { "Phase 2C accepted old state 必须是 LINK" }
        require(record.sourceType == CmsListItemSourceType.ARTICLE) { "Phase 2C accepted target state 必须由 current canonical item 表达为 ARTICLE" }
        val list = listMapper.findByCode(loaded.listCode) ?: error("Compatibility target List 不存在：${loaded.listCode}")
        require(list.enabled) { "Compatibility target List 已停用：${loaded.listCode}" }
        val current = listMapper.findItem(existing.listItemId) ?: error("Compatibility Runtime item 不存在：${existing.listItemId}")
        val image = requireNotNull(loaded.image) { "Compatibility current target 缺少 image" }
        val staticTarget = requireNotNull(loaded.staticTarget ?: acceptedOldStaticTarget(image.canonical.snapshotPath, image.canonical.sha256))
        val sourceProvenance = loaded.sourceProvenanceUrl ?: loaded.sourcePage.orEmpty()

        require(current.id == existing.listItemId && current.listId == list.id) { "Compatibility Runtime list identity 已漂移" }
        require(current.sourceType == transition.fromSourceType && current.articleId == null) { "Compatibility Runtime source type 已漂移" }
        require(current.title == record.title && current.subtitle == record.subtitle) { "Compatibility Runtime title/subtitle 已漂移" }
        require(current.url == sourceProvenance) { "Compatibility Runtime URL 已漂移" }
        require(current.imagePath == "/static/$staticTarget" && current.imageResourceId == null) { "Compatibility Runtime image projection 已漂移" }
        require(current.openMode == record.openMode && current.sortOrder == record.sourceOrder && current.enabled == record.enabled) { "Compatibility Runtime order/open/enabled 已漂移" }
        require(existing.sourceUrl == sourceProvenance) { "Compatibility mapping source provenance 已漂移" }
        require(existing.imageSourceUrl == image.canonical.sourceUrl && existing.imageSha256 == image.canonical.sha256) { "Compatibility mapping image evidence 已漂移" }
        val oldStatic = try {
            staticResourceService.resolvePublic(staticTarget)
        } catch (_: StaticResourceNotFoundException) {
            null
        }
        require(oldStatic != null && Files.isRegularFile(oldStatic)) { "Compatibility accepted old static image 不存在" }
        require(Files.size(oldStatic) == image.canonical.sizeBytes) { "Compatibility accepted old static image size 已漂移" }
        require(digest(oldStatic) == image.canonical.sha256) { "Compatibility accepted old static image SHA-256 已漂移" }

        val reference = requireNotNull(record.articleReference) { "Compatibility current ARTICLE target 缺少 stable reference" }
        val targetArticle = articleMapping.find(reference.sourceSystem, reference.legacyKey)
            ?: error("Compatibility target Article mapping 不存在：${reference.sourceSystem}/${reference.legacyKey}")
        val imageResourceId = resourceService.upload(
            PartyPathMultipartFile(sourceFilename(image.canonical.sourceUrl, image.canonical.sha256, image.canonical.snapshotPath), image.canonical.contentType, image.file),
        ).id
        val extraJson = objectMapper.writeValueAsString(
            mapOf(
                "migrationSourceSystem" to loaded.sourceSystem,
                "migrationSourcePage" to loaded.sourcePage,
                "legacyKey" to record.legacyKey,
                "sourceFingerprint" to record.sourceFingerprint,
                "imageSourceUrl" to image.canonical.sourceUrl,
                "imageSha256" to image.canonical.sha256,
                "sourceType" to record.sourceType.name,
                "articleLegacyKey" to reference.legacyKey,
            ),
        )
        val runtime = CmsListItemRecord(
            id = existing.listItemId,
            listId = requireNotNull(list.id),
            sourceType = record.sourceType.name,
            articleId = targetArticle.articleId,
            title = record.title,
            subtitle = record.subtitle,
            url = null,
            imagePath = null,
            imageResourceId = imageResourceId,
            openMode = record.openMode,
            sortOrder = record.sourceOrder,
            enabled = record.enabled,
            extraJson = extraJson,
        )
        val mapping = CmsListItemLegacyMappingRecord(
            id = existing.id,
            sourceSystem = loaded.sourceSystem,
            legacyKey = record.legacyKey,
            sourceUrl = sourceProvenance,
            sourceFingerprint = record.sourceFingerprint,
            imageSourceUrl = image.canonical.sourceUrl,
            imageSha256 = image.canonical.sha256,
            listItemId = existing.listItemId,
        )
        return PreparedCompatibilityUpdate(runtime, mapping)
    }

    private fun acceptedOldStaticTarget(snapshotPath: String, sha256: String): String {
        val extension = snapshotPath.substringAfterLast('.', "").lowercase()
        return CanonicalFileVerifier.validateStaticTarget("$PARTY_STATIC_PROJECTION_PREFIX/$sha256.$extension")
    }

    private fun digest(path: Path): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun sourceFilename(sourceUrl: String, sha: String, snapshotPath: String): String {
        val fromUrl = runCatching { Path.of(URI(sourceUrl).path).fileName?.toString() }.getOrNull().orEmpty()
        val fromSnapshot = runCatching { Path.of(snapshotPath).fileName?.toString() }.getOrNull().orEmpty()
        return fromUrl.ifBlank { fromSnapshot }.ifBlank { "$sha.bin" }.takeLast(255)
    }
}

data class PreparedCompatibilityUpdate(
    val runtime: CmsListItemRecord,
    val mapping: CmsListItemLegacyMappingRecord,
)

@Service
class PartyMigrationFacade(
    private val adapter: PartyCanonicalAdapter,
    private val generic: GenericContentMigrationService,
    private val compatibilityService: PartyCompatibilityService,
    private val listItemMapping: CmsListItemLegacyMappingMapper,
) {
    fun importArticles(snapshotRoot: Path): PartySnapshotImportReport {
        val dataset = runCatching { adapter.loadArticles(snapshotRoot) }.getOrElse { error ->
            return PartySnapshotImportReport(
                total = 0,
                created = 0,
                skipped = 0,
                conflicts = 0,
                invalid = 1,
                results = listOf(PartyRecordImportResult("dataset", PartyRecordImportStatus.INVALID, message = error.message ?: error::class.java.simpleName)),
            )
        }
        val report = generic.importPreparedDataset(dataset)
        val results = report.results.filter { it.kind == GenericMigrationKind.ARTICLE }.map(::articleResult)
        return PartySnapshotImportReport(
            total = dataset.articles.size,
            created = results.count { it.status == PartyRecordImportStatus.CREATED },
            skipped = results.count { it.status == PartyRecordImportStatus.SKIPPED },
            conflicts = results.count { it.status == PartyRecordImportStatus.CONFLICT },
            invalid = results.count { it.status == PartyRecordImportStatus.INVALID } + report.results.count { it.kind == GenericMigrationKind.DATASET && it.status == GenericMigrationStatus.INVALID },
            results = if (results.isNotEmpty()) results else report.results.filter { it.kind == GenericMigrationKind.DATASET }.map {
                PartyRecordImportResult(it.legacyKey, PartyRecordImportStatus.INVALID, it.runtimeId, it.message)
            },
        )
    }

    fun importCarousel(snapshotRoot: Path): PartyCarouselPlacementImportReport {
        val prepared = runCatching { adapter.loadCarousel(snapshotRoot) }.getOrElse { error ->
            return PartyCarouselPlacementImportReport(
                total = 0,
                created = 0,
                updated = 0,
                skipped = 0,
                conflicts = 0,
                invalid = 1,
                results = listOf(PartyCarouselPlacementImportResult("dataset", PartyCarouselPlacementImportStatus.INVALID, message = error.message ?: error::class.java.simpleName)),
            )
        }
        val compatibility = compatibilityService.apply(prepared.dataset, prepared.compatibility)
        val genericReport = generic.importPreparedDataset(prepared.dataset)
        val genericByKey = genericReport.results
            .filter { it.kind == GenericMigrationKind.LIST_ITEM }
            .associateBy { it.legacyKey }
        val results = prepared.dataset.listItems.map { loaded ->
            val key = loaded.record.legacyKey
            compatibility.updated[key]?.let { return@map PartyCarouselPlacementImportResult(key, PartyCarouselPlacementImportStatus.UPDATED, it) }
            compatibility.conflicts[key]?.let { return@map PartyCarouselPlacementImportResult(key, PartyCarouselPlacementImportStatus.CONFLICT, it.runtimeId, it.message) }
            genericByKey[key]?.let { return@map carouselResult(it) }
            val mapping = listItemMapping.find(loaded.sourceSystem, key)
            if (mapping?.sourceFingerprint == loaded.record.sourceFingerprint) {
                PartyCarouselPlacementImportResult(key, PartyCarouselPlacementImportStatus.SKIPPED, mapping.listItemId)
            } else {
                PartyCarouselPlacementImportResult(key, PartyCarouselPlacementImportStatus.INVALID, mapping?.listItemId, "Generic preflight 未产生可执行结果")
            }
        }.toMutableList()
        genericReport.results.filter { it.kind == GenericMigrationKind.DATASET && it.status == GenericMigrationStatus.INVALID }.forEach {
            results += PartyCarouselPlacementImportResult(it.legacyKey, PartyCarouselPlacementImportStatus.INVALID, it.runtimeId, it.message)
        }
        return PartyCarouselPlacementImportReport(
            total = prepared.dataset.listItems.size,
            created = results.count { it.status == PartyCarouselPlacementImportStatus.CREATED },
            updated = results.count { it.status == PartyCarouselPlacementImportStatus.UPDATED },
            skipped = results.count { it.status == PartyCarouselPlacementImportStatus.SKIPPED },
            conflicts = results.count { it.status == PartyCarouselPlacementImportStatus.CONFLICT },
            invalid = results.count { it.status == PartyCarouselPlacementImportStatus.INVALID },
            results = results,
        )
    }

    private fun articleResult(result: GenericMigrationResult) = PartyRecordImportResult(
        legacyKey = result.legacyKey,
        status = PartyRecordImportStatus.valueOf(result.status.name),
        articleId = result.runtimeId,
        message = result.message,
    )

    private fun carouselResult(result: GenericMigrationResult) = PartyCarouselPlacementImportResult(
        legacyKey = result.legacyKey,
        status = PartyCarouselPlacementImportStatus.valueOf(result.status.name),
        listItemId = result.runtimeId,
        message = result.message,
    )
}

private class PartyPathMultipartFile(
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
