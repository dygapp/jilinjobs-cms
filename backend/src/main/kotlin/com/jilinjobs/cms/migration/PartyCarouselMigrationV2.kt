package com.jilinjobs.cms.migration

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.listing.CmsListItemDraft
import com.jilinjobs.cms.listing.CmsListItemSourceType
import com.jilinjobs.cms.listing.CmsListService
import com.jilinjobs.cms.resource.ResourceService
import com.jilinjobs.cms.staticresource.StaticResourceService
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
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

private const val EU30_PARTY_CAROUSEL_CODE = "PARTY_CAROUSEL"
private const val EU30_POSITION2_LEGACY_KEY = "party-carousel:position:2"
private const val EU29_POSITION2_FINGERPRINT = "c2ad182b8b2dc981a3cbe3b0153a1e3e47604c1f01dd43e6d25971e1deed10dc"
private const val EU30_POSITION2_FINGERPRINT = "f8b5d8df87021373803639b174bf88e46ae6cef7f2599a205763b5887c78be84"
private val EU30_CAROUSEL_SHA256 = Regex("[0-9a-f]{64}")
private val EU30_CAROUSEL_IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp")
private val EU30_CAROUSEL_OPEN_MODES = setOf("DEFAULT", "SAME_WINDOW", "NEW_WINDOW")

data class PartyCarouselArticleRef(
    val sourceSystem: String,
    val legacyKey: String,
)

data class PartyCarouselPlacementItem(
    val legacyKey: String,
    val sourceOrder: Int,
    val sourceType: CmsListItemSourceType = CmsListItemSourceType.LINK,
    val title: String,
    // ARTICLE 项保留 legacy URL 仅用于来源追溯；Runtime URL 由文章关系计算。
    val url: String? = null,
    val articleRef: PartyCarouselArticleRef? = null,
    val openMode: String,
    val sourceFingerprint: String,
    val image: PartyCarouselSnapshotImage,
    val evidence: PartyCarouselSnapshotEvidence,
)

@Mapper
interface Eu30CarouselMappingMapper {
    @Select(
        """
        SELECT id, source_system, legacy_key, source_url, source_fingerprint,
               image_source_url, image_sha256, list_item_id
        FROM cms_list_item_legacy_mapping
        WHERE source_system=#{sourceSystem} AND legacy_key=#{legacyKey}
        """,
    )
    fun find(
        @Param("sourceSystem") sourceSystem: String,
        @Param("legacyKey") legacyKey: String,
    ): CmsListItemLegacyMappingRecord?

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

enum class PartyCarouselPlacementImportStatus { CREATED, UPDATED, SKIPPED, CONFLICT, INVALID }

data class PartyCarouselPlacementImportResult(
    val legacyKey: String,
    val status: PartyCarouselPlacementImportStatus,
    val listItemId: Long? = null,
    val message: String? = null,
)

data class PartyCarouselPlacementImportReport(
    val total: Int,
    val created: Int,
    val updated: Int,
    val skipped: Int,
    val conflicts: Int,
    val invalid: Int,
    val results: List<PartyCarouselPlacementImportResult>,
)

@Service
class PartyCarouselPlacementImporter(
    private val mappingMapper: CmsListItemLegacyMappingMapper,
    private val correctionMapper: Eu30CarouselMappingMapper,
    private val articleMappingMapper: ArticleLegacyMappingMapper,
    private val listService: CmsListService,
    private val staticResourceService: StaticResourceService,
    private val resourceService: ResourceService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun importItem(
        itemRoot: Path,
        sourceSystem: String,
        sourcePage: String,
        listId: Long,
        item: PartyCarouselPlacementItem,
    ): PartyCarouselPlacementImportResult {
        validateItem(sourceSystem, item)
        val existing = correctionMapper.find(sourceSystem, item.legacyKey)
        if (existing != null && existing.sourceFingerprint == item.sourceFingerprint) {
            return PartyCarouselPlacementImportResult(item.legacyKey, PartyCarouselPlacementImportStatus.SKIPPED, existing.listItemId)
        }
        if (existing != null && !isAcceptedPosition2Upgrade(existing, item)) {
            return PartyCarouselPlacementImportResult(
                item.legacyKey,
                PartyCarouselPlacementImportStatus.CONFLICT,
                existing.listItemId,
                "legacy carousel identity 已导入，但 source fingerprint 已变化",
            )
        }

        val draft = when (item.sourceType) {
            CmsListItemSourceType.LINK -> linkDraft(itemRoot, sourceSystem, sourcePage, item)
            CmsListItemSourceType.ARTICLE -> articleDraft(itemRoot, sourceSystem, sourcePage, item)
        }
        val listItem = if (existing == null) {
            listService.createItem(listId, draft)
        } else {
            listService.updateItem(listId, existing.listItemId, draft)
        }

        val mapping = CmsListItemLegacyMappingRecord(
            id = existing?.id,
            sourceSystem = sourceSystem,
            legacyKey = item.legacyKey,
            sourceUrl = item.url ?: sourcePage,
            sourceFingerprint = item.sourceFingerprint,
            imageSourceUrl = item.image.sourceUrl,
            imageSha256 = item.image.sha256,
            listItemId = listItem.id,
        )
        if (existing == null) mappingMapper.insert(mapping) else correctionMapper.update(mapping)
        return PartyCarouselPlacementImportResult(
            item.legacyKey,
            if (existing == null) PartyCarouselPlacementImportStatus.CREATED else PartyCarouselPlacementImportStatus.UPDATED,
            listItem.id,
        )
    }

    private fun isAcceptedPosition2Upgrade(
        existing: CmsListItemLegacyMappingRecord,
        item: PartyCarouselPlacementItem,
    ): Boolean =
        item.legacyKey == EU30_POSITION2_LEGACY_KEY &&
            item.sourceType == CmsListItemSourceType.ARTICLE &&
            existing.sourceFingerprint == EU29_POSITION2_FINGERPRINT &&
            item.sourceFingerprint == EU30_POSITION2_FINGERPRINT

    private fun linkDraft(
        itemRoot: Path,
        sourceSystem: String,
        sourcePage: String,
        item: PartyCarouselPlacementItem,
    ): CmsListItemDraft {
        val url = requireNotNull(item.url) { "LINK 轮播项必须提供 URL" }
        val imageFile = verifiedSnapshotImage(itemRoot, item.image)
        val extension = item.image.snapshotPath.substringAfterLast('.', "").lowercase()
        require(extension in EU30_CAROUSEL_IMAGE_EXTENSIONS) { "轮播图片扩展名不支持：$extension" }
        val staticRelativePath = "migrated/party/carousel/${item.image.sha256}.$extension"
        staticResourceService.upload(
            staticRelativePath,
            Eu30CarouselMultipartFile(imageFile.fileName.toString(), item.image.contentType, imageFile),
            true,
        )
        return CmsListItemDraft(
            sourceType = CmsListItemSourceType.LINK,
            title = item.title,
            url = url,
            imagePath = "/static/$staticRelativePath",
            openMode = item.openMode,
            sortOrder = item.sourceOrder,
            enabled = true,
            extraJson = migrationExtra(sourceSystem, sourcePage, item),
        )
    }

    private fun articleDraft(
        itemRoot: Path,
        sourceSystem: String,
        sourcePage: String,
        item: PartyCarouselPlacementItem,
    ): CmsListItemDraft {
        val reference = requireNotNull(item.articleRef) { "ARTICLE 轮播项缺少 articleRef" }
        val articleMapping = articleMappingMapper.find(reference.sourceSystem, reference.legacyKey)
            ?: error("ARTICLE 轮播项依赖的文章尚未导入：${reference.sourceSystem}/${reference.legacyKey}")
        val imageFile = verifiedSnapshotImage(itemRoot, item.image)
        val uploaded = resourceService.upload(
            Eu30CarouselMultipartFile(imageFile.fileName.toString(), item.image.contentType, imageFile),
        )
        return CmsListItemDraft(
            sourceType = CmsListItemSourceType.ARTICLE,
            articleId = articleMapping.articleId,
            title = item.title,
            imageResourceId = uploaded.id,
            openMode = item.openMode,
            sortOrder = item.sourceOrder,
            enabled = true,
            extraJson = migrationExtra(sourceSystem, sourcePage, item),
        )
    }

    private fun migrationExtra(sourceSystem: String, sourcePage: String, item: PartyCarouselPlacementItem) =
        objectMapper.writeValueAsString(
            mapOf(
                "migrationSourceSystem" to sourceSystem,
                "migrationSourcePage" to sourcePage,
                "legacyKey" to item.legacyKey,
                "sourceFingerprint" to item.sourceFingerprint,
                "imageSourceUrl" to item.image.sourceUrl,
                "imageSha256" to item.image.sha256,
                "sourceType" to item.sourceType.name,
                "articleLegacyKey" to item.articleRef?.legacyKey,
            ),
        )

    private fun validateItem(sourceSystem: String, item: PartyCarouselPlacementItem) {
        require(sourceSystem.isNotBlank() && sourceSystem.length <= 100) { "source system 不合法" }
        require(item.legacyKey == "party-carousel:position:${item.sourceOrder}") { "轮播 legacy identity 与原始顺序不一致" }
        require(item.sourceOrder in 1..4) { "轮播 sourceOrder 必须为 1..4" }
        require(item.title.isNotBlank() && item.title.length <= 200) { "轮播标题不合法" }
        require(item.openMode in EU30_CAROUSEL_OPEN_MODES) { "轮播打开方式不合法" }
        require(item.sourceFingerprint.matches(EU30_CAROUSEL_SHA256)) { "轮播 source fingerprint 不合法" }
        item.url?.let { validateHttpUrl(it, "legacy 轮播 URL") }
        validateHttpUrl(item.image.sourceUrl, "轮播图片来源 URL")
        require(item.image.sha256.matches(EU30_CAROUSEL_SHA256)) { "轮播图片 SHA-256 不合法" }
        require(item.image.sizeBytes > 0) { "轮播图片不能为空" }
        require(item.image.contentType?.startsWith("image/") == true) { "轮播资源不是图片" }
        when (item.sourceType) {
            CmsListItemSourceType.LINK -> require(item.articleRef == null && item.url != null) { "LINK 轮播项数据不完整" }
            CmsListItemSourceType.ARTICLE -> {
                val ref = requireNotNull(item.articleRef) { "ARTICLE 轮播项缺少 articleRef" }
                require(ref.sourceSystem.isNotBlank() && ref.legacyKey.isNotBlank()) { "ARTICLE 稳定引用不完整" }
            }
        }
    }

    private fun validateHttpUrl(value: String, label: String) {
        require(value.length <= 2000) { "$label 过长" }
        val uri = runCatching { URI(value) }.getOrNull()
        require(uri != null && uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank()) { "$label 不合法" }
    }

    private fun verifiedSnapshotImage(itemRoot: Path, image: PartyCarouselSnapshotImage): Path {
        val root = itemRoot.toAbsolutePath().normalize()
        val file = root.resolve(image.snapshotPath).normalize()
        require(file.startsWith(root) && Files.isRegularFile(file)) { "Snapshot 轮播图片不存在或路径越界：${image.snapshotPath}" }
        require(Files.size(file) == image.sizeBytes) { "Snapshot 轮播图片大小不一致：${image.snapshotPath}" }
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
        require(actualHash == image.sha256) { "Snapshot 轮播图片 SHA-256 不一致：${image.snapshotPath}" }
        return file
    }
}

@Service
class PartyCarouselMigrationV2Service(
    private val objectMapper: ObjectMapper,
    private val listService: CmsListService,
    private val placementImporter: PartyCarouselPlacementImporter,
    private val legacyService: PartyCarouselMigrationService,
) {
    fun importSnapshot(snapshotRoot: Path): PartyCarouselPlacementImportReport {
        val canonicalIndexFile = snapshotRoot.resolve("lists/$EU30_PARTY_CAROUSEL_CODE/index.json")
        if (!Files.isRegularFile(canonicalIndexFile)) {
            val legacy = legacyService.importSnapshot(snapshotRoot)
            return PartyCarouselPlacementImportReport(
                total = legacy.total,
                created = legacy.created,
                updated = 0,
                skipped = legacy.skipped,
                conflicts = legacy.conflicts,
                invalid = legacy.invalid,
                results = legacy.results.map {
                    PartyCarouselPlacementImportResult(
                        it.legacyKey,
                        PartyCarouselPlacementImportStatus.valueOf(it.status.name),
                        it.listItemId,
                        it.message,
                    )
                },
            )
        }

        val listRoot = canonicalIndexFile.parent
        val canonicalIndex = Files.newBufferedReader(canonicalIndexFile).use { reader ->
            objectMapper.readValue(reader, PartyCarouselCanonicalIndex::class.java)
        }
        require(canonicalIndex.listCode == EU30_PARTY_CAROUSEL_CODE) { "Canonical carousel 只允许 $EU30_PARTY_CAROUSEL_CODE" }
        val list = listService.listDefinitions().singleOrNull { it.code == EU30_PARTY_CAROUSEL_CODE }
            ?: error("未找到稳定列表：$EU30_PARTY_CAROUSEL_CODE")
        require(list.enabled) { "$EU30_PARTY_CAROUSEL_CODE 已停用" }

        val items = canonicalIndex.items.sortedBy { it.sourceOrder }.map { reference ->
            val root = listRoot.toAbsolutePath().normalize()
            val itemFile = root.resolve(reference.path).normalize()
            require(itemFile.startsWith(root) && Files.isRegularFile(itemFile)) { "Canonical carousel item 不存在或路径越界：${reference.path}" }
            val item = Files.newBufferedReader(itemFile).use { reader -> objectMapper.readValue(reader, PartyCarouselPlacementItem::class.java) }
            require(item.legacyKey == reference.legacyKey) { "Canonical carousel legacyKey 与 item.json 不一致：${reference.legacyKey}" }
            require(item.sourceOrder == reference.sourceOrder) { "Canonical carousel sourceOrder 与 item.json 不一致：${reference.legacyKey}" }
            require(item.sourceFingerprint == reference.sourceFingerprint) { "Canonical carousel fingerprint 与 item.json 不一致：${reference.legacyKey}" }
            itemFile.parent to item
        }
        require(items.size == 4 && items.map { it.second.sourceOrder }.toSet() == setOf(1, 2, 3, 4)) { "党建历史轮播必须完整覆盖 1..4" }

        val results = items.map { (itemRoot, item) ->
            runCatching {
                placementImporter.importItem(itemRoot, canonicalIndex.sourceSystem, canonicalIndex.sourcePage, list.id, item)
            }.getOrElse { error ->
                PartyCarouselPlacementImportResult(item.legacyKey, PartyCarouselPlacementImportStatus.INVALID, message = error.message ?: error::class.java.simpleName)
            }
        }
        return PartyCarouselPlacementImportReport(
            total = results.size,
            created = results.count { it.status == PartyCarouselPlacementImportStatus.CREATED },
            updated = results.count { it.status == PartyCarouselPlacementImportStatus.UPDATED },
            skipped = results.count { it.status == PartyCarouselPlacementImportStatus.SKIPPED },
            conflicts = results.count { it.status == PartyCarouselPlacementImportStatus.CONFLICT },
            invalid = results.count { it.status == PartyCarouselPlacementImportStatus.INVALID },
            results = results,
        )
    }
}

private class Eu30CarouselMultipartFile(
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
    require(args.isNotEmpty()) { "用法：importPartyCarousel <snapshot-root> [Spring Boot args...]" }
    val snapshotRoot = Path.of(args.first()).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(CmsApplication::class.java).web(WebApplicationType.NONE).run(*args.drop(1).toTypedArray())
    try {
        val report = context.getBean(PartyCarouselMigrationV2Service::class.java).importSnapshot(snapshotRoot)
        println("EU29_CAROUSEL_IMPORT_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
        require(report.conflicts == 0 && report.invalid == 0) { "Party carousel import 存在 conflict/invalid，拒绝静默完成" }
    } finally {
        context.close()
    }
}