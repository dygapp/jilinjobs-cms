package com.jilinjobs.cms.migration

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.listing.CmsListItemDraft
import com.jilinjobs.cms.listing.CmsListService
import com.jilinjobs.cms.staticresource.StaticResourceService
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

private const val PARTY_CAROUSEL_CODE = "PARTY_CAROUSEL"
private val CAROUSEL_SHA256 = Regex("[0-9a-f]{64}")
private val CAROUSEL_IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp")
private val CAROUSEL_OPEN_MODES = setOf("DEFAULT", "SAME_WINDOW", "NEW_WINDOW")

@Mapper
interface CmsListItemLegacyMappingMapper {
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

    @Insert(
        """
        INSERT INTO cms_list_item_legacy_mapping(
            source_system, legacy_key, source_url, source_fingerprint,
            image_source_url, image_sha256, list_item_id
        ) VALUES(
            #{sourceSystem}, #{legacyKey}, #{sourceUrl}, #{sourceFingerprint},
            #{imageSourceUrl}, #{imageSha256}, #{listItemId}
        )
        """,
    )
    fun insert(record: CmsListItemLegacyMappingRecord): Int
}

data class CmsListItemLegacyMappingRecord(
    var id: Long? = null,
    var sourceSystem: String = "",
    var legacyKey: String = "",
    var sourceUrl: String = "",
    var sourceFingerprint: String = "",
    var imageSourceUrl: String = "",
    var imageSha256: String = "",
    var listItemId: Long = 0,
)

data class PartyCarouselSnapshotImage(
    val sourceUrl: String,
    val originalReference: String,
    val snapshotPath: String,
    val sha256: String,
    val contentType: String?,
    val sizeBytes: Long,
)

data class PartyCarouselSnapshotEvidence(
    val sourceHref: String,
    val sourceImageReference: String,
    val sourceTarget: String,
)

data class PartyCarouselSnapshotItem(
    val legacyKey: String,
    val sourceOrder: Int,
    val title: String,
    val url: String,
    val openMode: String,
    val sourceFingerprint: String,
    val image: PartyCarouselSnapshotImage,
    val evidence: PartyCarouselSnapshotEvidence,
)

data class PartyCarouselSnapshot(
    val listCode: String,
    val sourceSystem: String,
    val sourcePage: String,
    val items: List<PartyCarouselSnapshotItem>,
)

enum class PartyCarouselItemImportStatus { CREATED, SKIPPED, CONFLICT, INVALID }

data class PartyCarouselItemImportResult(
    val legacyKey: String,
    val status: PartyCarouselItemImportStatus,
    val listItemId: Long? = null,
    val message: String? = null,
)

data class PartyCarouselImportReport(
    val total: Int,
    val created: Int,
    val skipped: Int,
    val conflicts: Int,
    val invalid: Int,
    val results: List<PartyCarouselItemImportResult>,
)

@Service
class PartyCarouselItemImporter(
    private val mappingMapper: CmsListItemLegacyMappingMapper,
    private val listService: CmsListService,
    private val staticResourceService: StaticResourceService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun importItem(
        snapshotRoot: Path,
        sourceSystem: String,
        sourcePage: String,
        listId: Long,
        item: PartyCarouselSnapshotItem,
    ): PartyCarouselItemImportResult {
        validateItem(sourceSystem, item)
        val existing = mappingMapper.find(sourceSystem, item.legacyKey)
        if (existing != null) {
            return if (existing.sourceFingerprint == item.sourceFingerprint) {
                PartyCarouselItemImportResult(item.legacyKey, PartyCarouselItemImportStatus.SKIPPED, existing.listItemId)
            } else {
                PartyCarouselItemImportResult(
                    item.legacyKey,
                    PartyCarouselItemImportStatus.CONFLICT,
                    existing.listItemId,
                    "legacy carousel identity 已导入，但 source fingerprint 已变化",
                )
            }
        }

        val imageFile = verifiedSnapshotImage(snapshotRoot, item.image)
        val extension = item.image.snapshotPath.substringAfterLast('.', "").lowercase()
        require(extension in CAROUSEL_IMAGE_EXTENSIONS) { "轮播图片扩展名不支持：$extension" }
        val staticRelativePath = "migrated/party/carousel/${item.image.sha256}.$extension"
        staticResourceService.upload(
            staticRelativePath,
            CarouselSnapshotMultipartFile(imageFile.fileName.toString(), item.image.contentType, imageFile),
            true,
        )
        val imagePath = "/static/$staticRelativePath"
        val extraJson = objectMapper.writeValueAsString(
            mapOf(
                "migrationSourceSystem" to sourceSystem,
                "migrationSourcePage" to sourcePage,
                "legacyKey" to item.legacyKey,
                "sourceFingerprint" to item.sourceFingerprint,
                "imageSourceUrl" to item.image.sourceUrl,
                "imageSha256" to item.image.sha256,
            ),
        )
        val listItem = listService.createItem(
            listId,
            CmsListItemDraft(
                title = item.title,
                subtitle = null,
                url = item.url,
                imagePath = imagePath,
                openMode = item.openMode,
                sortOrder = item.sourceOrder,
                enabled = true,
                extraJson = extraJson,
            ),
        )
        mappingMapper.insert(
            CmsListItemLegacyMappingRecord(
                sourceSystem = sourceSystem,
                legacyKey = item.legacyKey,
                sourceUrl = item.url,
                sourceFingerprint = item.sourceFingerprint,
                imageSourceUrl = item.image.sourceUrl,
                imageSha256 = item.image.sha256,
                listItemId = listItem.id,
            ),
        )
        return PartyCarouselItemImportResult(item.legacyKey, PartyCarouselItemImportStatus.CREATED, listItem.id)
    }

    private fun validateItem(sourceSystem: String, item: PartyCarouselSnapshotItem) {
        require(sourceSystem.isNotBlank() && sourceSystem.length <= 100) { "source system 不合法" }
        require(item.legacyKey == "party-carousel:position:${item.sourceOrder}") { "轮播 legacy identity 与原始顺序不一致" }
        require(item.sourceOrder in 1..4) { "轮播 sourceOrder 必须为 1..4" }
        require(item.title.isNotBlank() && item.title.length <= 200) { "轮播标题不合法" }
        require(item.openMode in CAROUSEL_OPEN_MODES) { "轮播打开方式不合法" }
        require(item.sourceFingerprint.matches(CAROUSEL_SHA256)) { "轮播 source fingerprint 不合法" }
        validateHttpUrl(item.url, "轮播跳转 URL")
        validateHttpUrl(item.image.sourceUrl, "轮播图片来源 URL")
        require(item.image.sha256.matches(CAROUSEL_SHA256)) { "轮播图片 SHA-256 不合法" }
        require(item.image.sizeBytes > 0) { "轮播图片不能为空" }
        require(item.image.contentType?.startsWith("image/") == true) { "轮播资源不是图片" }
    }

    private fun validateHttpUrl(value: String, label: String) {
        require(value.length <= 2000) { "$label 过长" }
        val uri = runCatching { URI(value) }.getOrNull()
        require(uri != null && uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank()) { "$label 不合法" }
    }

    private fun verifiedSnapshotImage(snapshotRoot: Path, image: PartyCarouselSnapshotImage): Path {
        val root = snapshotRoot.toAbsolutePath().normalize()
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
class PartyCarouselMigrationService(
    private val objectMapper: ObjectMapper,
    private val listService: CmsListService,
    private val itemImporter: PartyCarouselItemImporter,
) {
    fun importSnapshot(snapshotRoot: Path): PartyCarouselImportReport {
        val file = snapshotRoot.resolve("carousel.json")
        require(Files.isRegularFile(file)) { "缺少 Snapshot carousel.json：$file" }
        val snapshot = Files.newBufferedReader(file).use { reader -> objectMapper.readValue(reader, PartyCarouselSnapshot::class.java) }
        require(snapshot.listCode == PARTY_CAROUSEL_CODE) { "EU-29 carousel snapshot 只允许 $PARTY_CAROUSEL_CODE" }
        require(snapshot.items.size == 4) { "党建历史轮播必须恰好 4 条，实际 ${snapshot.items.size}" }
        require(snapshot.items.map { it.sourceOrder }.toSet() == setOf(1, 2, 3, 4)) { "党建历史轮播顺序必须完整覆盖 1..4" }
        val list = listService.listDefinitions().singleOrNull { it.code == PARTY_CAROUSEL_CODE }
            ?: error("未找到稳定列表：$PARTY_CAROUSEL_CODE")
        require(list.enabled) { "$PARTY_CAROUSEL_CODE 已停用" }

        val results = snapshot.items.sortedBy { it.sourceOrder }.map { item ->
            runCatching {
                itemImporter.importItem(snapshotRoot, snapshot.sourceSystem, snapshot.sourcePage, list.id, item)
            }.getOrElse { error ->
                PartyCarouselItemImportResult(item.legacyKey, PartyCarouselItemImportStatus.INVALID, message = error.message ?: error::class.java.simpleName)
            }
        }
        return PartyCarouselImportReport(
            total = results.size,
            created = results.count { it.status == PartyCarouselItemImportStatus.CREATED },
            skipped = results.count { it.status == PartyCarouselItemImportStatus.SKIPPED },
            conflicts = results.count { it.status == PartyCarouselItemImportStatus.CONFLICT },
            invalid = results.count { it.status == PartyCarouselItemImportStatus.INVALID },
            results = results,
        )
    }
}

private class CarouselSnapshotMultipartFile(
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
        val report = context.getBean(PartyCarouselMigrationService::class.java).importSnapshot(snapshotRoot)
        println("EU29_CAROUSEL_IMPORT_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
        require(report.conflicts == 0 && report.invalid == 0) { "EU-29 carousel import 存在 conflict/invalid，拒绝静默完成" }
    } finally {
        context.close()
    }
}
