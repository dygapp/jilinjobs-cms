package com.jilinjobs.cms

import com.jilinjobs.cms.content.ArticleType
import com.jilinjobs.cms.listing.CmsListItemSourceType
import com.jilinjobs.cms.migration.generic.CanonicalArticleContent
import com.jilinjobs.cms.migration.generic.CanonicalArticleEvidence
import com.jilinjobs.cms.migration.generic.CanonicalArticleIndexEntry
import com.jilinjobs.cms.migration.generic.CanonicalArticleRecord
import com.jilinjobs.cms.migration.generic.CanonicalArticleReference
import com.jilinjobs.cms.migration.generic.CanonicalArticleSource
import com.jilinjobs.cms.migration.generic.CanonicalArticleTarget
import com.jilinjobs.cms.migration.generic.CanonicalCompatibilityAuthority
import com.jilinjobs.cms.migration.generic.CanonicalListImage
import com.jilinjobs.cms.migration.generic.CanonicalListIndex
import com.jilinjobs.cms.migration.generic.CanonicalListItemCompatibilityTransition
import com.jilinjobs.cms.migration.generic.CanonicalListItemRecord
import com.jilinjobs.cms.migration.generic.CanonicalListItemReference
import com.jilinjobs.cms.migration.generic.GenericContentMigrationService
import com.jilinjobs.cms.migration.generic.GenericMigrationPhase
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.Connection
import javax.sql.DataSource
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import tools.jackson.databind.ObjectMapper

private data class CompatibilityFixture(
    val listCode: String,
    val columnAlias: String,
    val sourceSystem: String,
    val listLegacyKey: String,
    val articleLegacyKey: String,
    val oldFingerprint: String,
    val newFingerprint: String,
    val oldUrl: String,
    val staticTarget: String,
)

private data class RuntimeState(
    val id: Long,
    val sourceType: String,
    val articleId: Long?,
    val url: String?,
    val imagePath: String?,
    val imageResourceId: Long?,
    val sourceFingerprint: String,
)

fun main() {
    val dbUrl = requireNotNull(System.getenv("GENERIC_COMPATIBILITY_VERIFY_DB_URL")) {
        "GENERIC_COMPATIBILITY_VERIFY_DB_URL is required"
    }
    val workspace = Files.createTempDirectory("generic-list-item-compatibility-")
    val storageRoot = workspace.resolve("uploads")
    val staticRoot = workspace.resolve("static")
    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=" + (System.getenv("GENERIC_COMPATIBILITY_VERIFY_DB_USERNAME") ?: "root"),
            "--spring.datasource.password=" + (System.getenv("GENERIC_COMPATIBILITY_VERIFY_DB_PASSWORD") ?: "root"),
            "--cms.storage.root=$storageRoot",
            "--cms.static.root=$staticRoot",
            "--spring.main.banner-mode=off",
        )
    try {
        val service = context.getBean(GenericContentMigrationService::class.java)
        val mapper = context.getBean(ObjectMapper::class.java)
        val dataSource = context.getBean(DataSource::class.java)

        verifySuccess(workspace.resolve("success"), service, mapper, dataSource)
        verifyConflict(workspace.resolve("conflict"), service, mapper, dataSource)

        println("GENERIC_LIST_ITEM_COMPATIBILITY_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun verifySuccess(
    root: Path,
    service: GenericContentMigrationService,
    mapper: ObjectMapper,
    dataSource: DataSource,
) {
    val fixture = fixture("SUCCESS")
    dataSource.connection.use { seedTargets(it, fixture) }
    val oldRoot = root.resolve("old")
    val currentRoot = root.resolve("current")
    writeOldDataset(oldRoot, fixture, mapper)
    writeCurrentDataset(currentRoot, fixture, mapper)

    val first = service.importSnapshot(oldRoot)
    require(first.phase == GenericMigrationPhase.EXECUTE && first.total == 1 && first.created == 1 && first.updated == 0 && first.conflicts == 0) {
        "Old canonical import failed: " + first
    }
    val before = dataSource.connection.use { state(it, fixture) }
    require(before.sourceType == "LINK" && before.articleId == null && before.imageResourceId == null) {
        "Old Runtime is not LINK/static: " + before
    }

    val transitioned = service.importSnapshot(currentRoot)
    require(transitioned.phase == GenericMigrationPhase.EXECUTE && transitioned.total == 2 && transitioned.created == 1 && transitioned.updated == 1 && transitioned.conflicts == 0 && transitioned.invalid == 0) {
        "Compatibility transition report unexpected: " + transitioned
    }
    val after = dataSource.connection.use { state(it, fixture) }
    require(after.id == before.id) { "Compatibility transition changed Runtime id" }
    require(after.sourceType == "ARTICLE" && after.articleId != null && after.url == null) {
        "Compatibility transition did not produce ARTICLE Runtime: " + after
    }
    require(after.imagePath == null && after.imageResourceId != null) {
        "Compatibility transition did not move image to managed Resource: " + after
    }
    require(after.sourceFingerprint == fixture.newFingerprint) {
        "Compatibility transition did not update fingerprint"
    }

    val second = service.importSnapshot(currentRoot)
    require(second.phase == GenericMigrationPhase.EXECUTE && second.total == 2 && second.updated == 0 && second.skipped == 2 && second.conflicts == 0 && second.invalid == 0) {
        "Second import is not idempotent: " + second
    }
    require(dataSource.connection.use { state(it, fixture) } == after) {
        "Idempotent re-import changed Runtime"
    }
}

private fun verifyConflict(
    root: Path,
    service: GenericContentMigrationService,
    mapper: ObjectMapper,
    dataSource: DataSource,
) {
    val fixture = fixture("CONFLICT")
    dataSource.connection.use { seedTargets(it, fixture) }
    val oldRoot = root.resolve("old")
    val currentRoot = root.resolve("current")
    writeOldDataset(oldRoot, fixture, mapper)
    writeCurrentDataset(currentRoot, fixture, mapper)

    val first = service.importSnapshot(oldRoot)
    require(first.created == 1 && first.conflicts == 0 && first.invalid == 0) { "Conflict fixture old import failed: " + first }
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            val changed = statement.executeUpdate(
                "UPDATE cms_list_item SET url='https://tampered.invalid/runtime' WHERE id=(SELECT list_item_id FROM cms_list_item_legacy_mapping WHERE source_system='" +
                    fixture.sourceSystem + "' AND legacy_key='" + fixture.listLegacyKey + "')",
            )
            require(changed == 1) { "Conflict fixture tamper failed" }
        }
    }
    val before = dataSource.connection.use { state(it, fixture) }
    val current = service.importSnapshot(currentRoot)
    require(current.phase == GenericMigrationPhase.PREFLIGHT && current.created == 0 && current.updated == 0 && current.conflicts == 1 && current.invalid == 0) {
        "Drift must fail during compatibility preflight: " + current
    }
    require(dataSource.connection.use { state(it, fixture) } == before) {
        "Conflict preflight mutated Runtime"
    }
}

private fun fixture(suffix: String): CompatibilityFixture {
    val lower = suffix.lowercase()
    val imageBytes = pngBytes(suffix.hashCode().toByte())
    val imageSha = sha256(imageBytes)
    return CompatibilityFixture(
        listCode = "COMPAT_" + suffix,
        columnAlias = "compat-" + lower,
        sourceSystem = "generic-compat-" + lower,
        listLegacyKey = "list-item:" + lower,
        articleLegacyKey = "article:" + lower,
        oldFingerprint = sha256(("old-" + lower).toByteArray(StandardCharsets.UTF_8)),
        newFingerprint = sha256(("new-" + lower).toByteArray(StandardCharsets.UTF_8)),
        oldUrl = "https://example.invalid/" + lower + "/legacy",
        staticTarget = "migrated/verification/" + imageSha + ".png",
    )
}

private fun writeOldDataset(root: Path, fixture: CompatibilityFixture, mapper: ObjectMapper) {
    val bytes = pngBytes(fixture.listCode.hashCode().toByte())
    val sha = sha256(bytes)
    val item = CanonicalListItemRecord(
        legacyKey = fixture.listLegacyKey,
        sourceOrder = 1,
        sourceType = CmsListItemSourceType.LINK,
        title = "Compatibility " + fixture.listCode,
        url = fixture.oldUrl,
        sourceProvenanceUrl = fixture.oldUrl,
        openMode = "NEW_WINDOW",
        sourceFingerprint = fixture.oldFingerprint,
        image = CanonicalListImage(
            sourceUrl = "https://example.invalid/assets/" + fixture.listCode.lowercase() + ".png",
            snapshotPath = "assets/card.png",
            sha256 = sha,
            contentType = "image/png",
            sizeBytes = bytes.size.toLong(),
        ),
        staticTarget = fixture.staticTarget,
    )
    writeList(root, fixture, item, bytes, mapper)
}

private fun writeCurrentDataset(root: Path, fixture: CompatibilityFixture, mapper: ObjectMapper) {
    val bytes = pngBytes(fixture.listCode.hashCode().toByte())
    val sha = sha256(bytes)
    Files.createDirectories(root)

    val article = CanonicalArticleRecord(
        source = CanonicalArticleSource(
            system = fixture.sourceSystem,
            legacyKey = fixture.articleLegacyKey,
            contentId = null,
            typeCode = "compatibility",
            detailPath = "/compatibility/" + fixture.articleLegacyKey,
            url = "https://example.invalid/" + fixture.articleLegacyKey,
        ),
        target = CanonicalArticleTarget(fixture.columnAlias, ArticleType.EXTERNAL_LINK),
        content = CanonicalArticleContent(
            title = "Compatibility target " + fixture.listCode,
            source = "Generic compatibility verification",
            publishDate = null,
            bodyHtml = "",
            externalUrl = "https://example.invalid/target/" + fixture.articleLegacyKey,
        ),
        resources = emptyList(),
        sourceFingerprint = sha256(("article-" + fixture.articleLegacyKey).toByteArray(StandardCharsets.UTF_8)),
        evidence = CanonicalArticleEvidence(1, "Compatibility", null, 1),
    )
    val articlePath = root.resolve("articles/target/article.json")
    Files.createDirectories(articlePath.parent)
    Files.writeString(articlePath, mapper.writeValueAsString(article))
    Files.writeString(
        root.resolve("index.ndjson"),
        mapper.writeValueAsString(CanonicalArticleIndexEntry(fixture.articleLegacyKey, "articles/target/article.json")) + "\n",
    )

    val item = CanonicalListItemRecord(
        legacyKey = fixture.listLegacyKey,
        sourceOrder = 1,
        sourceType = CmsListItemSourceType.ARTICLE,
        title = "Compatibility " + fixture.listCode,
        sourceProvenanceUrl = fixture.oldUrl,
        articleReference = CanonicalArticleReference(fixture.sourceSystem, fixture.articleLegacyKey),
        openMode = "NEW_WINDOW",
        sourceFingerprint = fixture.newFingerprint,
        image = CanonicalListImage(
            sourceUrl = "https://example.invalid/assets/" + fixture.listCode.lowercase() + ".png",
            snapshotPath = "assets/card.png",
            sha256 = sha,
            contentType = "image/png",
            sizeBytes = bytes.size.toLong(),
        ),
    )
    writeList(root, fixture, item, bytes, mapper)
    Files.writeString(
        root.resolve("compatibility.json"),
        mapper.writeValueAsString(
            CanonicalCompatibilityAuthority(
                version = 1,
                listItemTransitions = listOf(
                    CanonicalListItemCompatibilityTransition(
                        listCode = fixture.listCode,
                        sourceSystem = fixture.sourceSystem,
                        legacyKey = fixture.listLegacyKey,
                        fromFingerprint = fixture.oldFingerprint,
                        fromSourceType = CmsListItemSourceType.LINK,
                        fromUrl = fixture.oldUrl,
                        fromImagePath = fixture.staticTarget,
                        preserveRuntimeId = true,
                    ),
                ),
            ),
        ),
    )
}

private fun writeList(
    root: Path,
    fixture: CompatibilityFixture,
    item: CanonicalListItemRecord,
    imageBytes: ByteArray,
    mapper: ObjectMapper,
) {
    val itemPath = "items/item/item.json"
    val listRoot = root.resolve("lists/" + fixture.listCode)
    val itemFile = listRoot.resolve(itemPath)
    Files.createDirectories(itemFile.parent.resolve("assets"))
    Files.write(itemFile.parent.resolve("assets/card.png"), imageBytes)
    Files.writeString(itemFile, mapper.writeValueAsString(item))
    Files.writeString(
        listRoot.resolve("index.json"),
        mapper.writeValueAsString(
            CanonicalListIndex(
                listCode = fixture.listCode,
                sourceSystem = fixture.sourceSystem,
                sourcePage = "https://example.invalid/" + fixture.listCode.lowercase(),
                items = listOf(
                    CanonicalListItemReference(
                        legacyKey = fixture.listLegacyKey,
                        path = itemPath,
                        sourceOrder = 1,
                        sourceFingerprint = item.sourceFingerprint,
                    ),
                ),
            ),
        ),
    )
}

private fun seedTargets(connection: Connection, fixture: CompatibilityFixture) {
    connection.createStatement().use { statement ->
        statement.executeUpdate(
            "INSERT INTO cms_column(alias,name,cover_policy,sort_order,enabled,preset) VALUES ('" +
                fixture.columnAlias + "','Compatibility','OPTIONAL',0,1,0)",
        )
        statement.executeUpdate(
            "INSERT INTO cms_list(code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset) VALUES ('" +
                fixture.listCode + "','Compatibility','GENERAL','OPTIONAL','',0,1,0,0)",
        )
    }
}

private fun state(connection: Connection, fixture: CompatibilityFixture): RuntimeState =
    connection.prepareStatement(
        """
        SELECT i.id,i.source_type,i.article_id,i.url,i.image_path,i.image_resource_id,m.source_fingerprint
        FROM cms_list_item i
        JOIN cms_list_item_legacy_mapping m ON m.list_item_id=i.id
        WHERE m.source_system=? AND m.legacy_key=?
        """.trimIndent(),
    ).use { statement ->
        statement.setString(1, fixture.sourceSystem)
        statement.setString(2, fixture.listLegacyKey)
        statement.executeQuery().use { result ->
            require(result.next()) { "Runtime fixture missing" }
            RuntimeState(
                id = result.getLong("id"),
                sourceType = result.getString("source_type"),
                articleId = result.getLong("article_id").takeUnless { result.wasNull() },
                url = result.getString("url"),
                imagePath = result.getString("image_path"),
                imageResourceId = result.getLong("image_resource_id").takeUnless { result.wasNull() },
                sourceFingerprint = result.getString("source_fingerprint"),
            )
        }
    }

private fun pngBytes(seed: Byte): ByteArray =
    byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, seed, 1, 2, 3)

private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(bytes)
    .joinToString("") { "%02x".format(it) }
