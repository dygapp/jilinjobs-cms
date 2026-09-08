package com.jilinjobs.cms

import com.jilinjobs.cms.content.ArticleType
import com.jilinjobs.cms.listing.CmsListItemSourceType
import com.jilinjobs.cms.migration.PartyCanonicalIndexEntry
import com.jilinjobs.cms.migration.PartyCarouselArticleRef
import com.jilinjobs.cms.migration.PartyCarouselCanonicalIndex
import com.jilinjobs.cms.migration.PartyCarouselCanonicalIndexItem
import com.jilinjobs.cms.migration.PartyCarouselPlacementImportStatus
import com.jilinjobs.cms.migration.PartyCarouselPlacementItem
import com.jilinjobs.cms.migration.PartyCarouselSnapshotEvidence
import com.jilinjobs.cms.migration.PartyCarouselSnapshotImage
import com.jilinjobs.cms.migration.PartyMigrationContent
import com.jilinjobs.cms.migration.PartyMigrationEvidence
import com.jilinjobs.cms.migration.PartyMigrationRecord
import com.jilinjobs.cms.migration.PartyMigrationSource
import com.jilinjobs.cms.migration.PartyMigrationTarget
import com.jilinjobs.cms.migration.party.PartyCompatibilityAuthority
import com.jilinjobs.cms.migration.party.PartyCompatibilityTransition
import com.jilinjobs.cms.migration.party.PartyContentScope
import com.jilinjobs.cms.migration.party.PartyManifest
import com.jilinjobs.cms.migration.party.PartyMigrationFacade
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.Connection
import javax.sql.DataSource
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import tools.jackson.databind.ObjectMapper

private enum class CompatibilityScenario {
    SUCCESS,
    WRONG_FROM_FINGERPRINT,
    WRONG_RUNTIME_SOURCE_TYPE,
    WRONG_RUNTIME_ORDER,
    WRONG_RUNTIME_URL,
    WRONG_RUNTIME_IMAGE_PATH,
    WRONG_RUNTIME_IMAGE_DIGEST,
    MISSING_TARGET_ARTICLE,
    MISSING_TRANSITION,
}

private data class RuntimeListState(
    val id: Long,
    val sourceType: String,
    val articleId: Long?,
    val url: String?,
    val imagePath: String?,
    val imageResourceId: Long?,
    val sortOrder: Int,
    val sourceFingerprint: String,
)

private data class Fixture(
    val oldRoot: Path,
    val currentRoot: Path,
    val sourceSystem: String,
    val listCode: String,
    val listLegacyKey: String,
    val oldFingerprint: String,
    val currentFingerprint: String,
    val articleLegacyKey: String,
)

fun main() {
    val dbUrl = requireNotNull(System.getenv("PARTY_MIGRATION_VERIFY_DB_URL")) {
        "PARTY_MIGRATION_VERIFY_DB_URL is required"
    }
    val workspace = Files.createTempDirectory("party-migration-despecialization-runtime-")
    val storageRoot = workspace.resolve("uploads")
    val staticRoot = workspace.resolve("static")
    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=${System.getenv("PARTY_MIGRATION_VERIFY_DB_USERNAME") ?: "root"}",
            "--spring.datasource.password=${System.getenv("PARTY_MIGRATION_VERIFY_DB_PASSWORD") ?: "root"}",
            "--cms.storage.root=$storageRoot",
            "--cms.static.root=$staticRoot",
            "--spring.main.banner-mode=off",
        )
    try {
        val facade = context.getBean(PartyMigrationFacade::class.java)
        val objectMapper = context.getBean(ObjectMapper::class.java)
        val dataSource = context.getBean(DataSource::class.java)
        CompatibilityScenario.entries.forEach { scenario ->
            verifyScenario(scenario, workspace.resolve(scenario.name.lowercase()), staticRoot, facade, objectMapper, dataSource)
        }
        println("PARTY_MIGRATION_COMPATIBILITY_RUNTIME_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun verifyScenario(
    scenario: CompatibilityScenario,
    root: Path,
    staticRoot: Path,
    facade: PartyMigrationFacade,
    objectMapper: ObjectMapper,
    dataSource: DataSource,
) {
    val fixture = writeFixture(root, scenario, objectMapper)
    dataSource.connection.use { seedTargets(it, fixture, scenario) }

    val accepted = facade.importCarousel(fixture.oldRoot)
    require(accepted.total == 1 && accepted.created == 1 && accepted.updated == 0 && accepted.conflicts == 0 && accepted.invalid == 0) {
        "$scenario old accepted LINK import failed: $accepted"
    }
    val acceptedState = dataSource.connection.use { state(it, fixture) }
    require(acceptedState.sourceType == "LINK" && acceptedState.articleId == null && acceptedState.imageResourceId == null) {
        "$scenario old accepted Runtime is not LINK/static: $acceptedState"
    }
    require(acceptedState.imagePath?.startsWith("/static/migrated/party/carousel/") == true) {
        "$scenario old accepted static projection changed: $acceptedState"
    }

    if (scenario != CompatibilityScenario.MISSING_TARGET_ARTICLE) {
        val article = facade.importArticles(fixture.currentRoot)
        require(article.total == 1 && article.created == 1 && article.conflicts == 0 && article.invalid == 0) {
            "$scenario Party Article adapter did not delegate a current Article create through Generic: $article"
        }
    }

    dataSource.connection.use { connection ->
        when (scenario) {
            CompatibilityScenario.WRONG_RUNTIME_SOURCE_TYPE -> updateItem(connection, fixture, "source_type='ARTICLE'")
            CompatibilityScenario.WRONG_RUNTIME_ORDER -> updateItem(connection, fixture, "sort_order=99")
            CompatibilityScenario.WRONG_RUNTIME_URL -> updateItem(connection, fixture, "url='https://tampered.invalid/runtime'")
            CompatibilityScenario.WRONG_RUNTIME_IMAGE_PATH -> updateItem(connection, fixture, "image_path='/static/tampered/path.png'")
            else -> Unit
        }
    }
    if (scenario == CompatibilityScenario.WRONG_RUNTIME_IMAGE_DIGEST) {
        val path = requireNotNull(acceptedState.imagePath).removePrefix("/static/")
        val file = staticRoot.resolve(path).normalize()
        require(file.startsWith(staticRoot.toAbsolutePath().normalize()) && Files.isRegularFile(file)) { "accepted static fixture missing: $file" }
        val bytes = Files.readAllBytes(file)
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 0x01).toByte()
        Files.write(file, bytes)
    }

    val guardedBefore = dataSource.connection.use { state(it, fixture) }
    val current = facade.importCarousel(fixture.currentRoot)
    if (scenario == CompatibilityScenario.SUCCESS) {
        require(current.total == 1 && current.created == 0 && current.updated == 1 && current.skipped == 0 && current.conflicts == 0 && current.invalid == 0) {
            "successful compatibility transition report unexpected: $current"
        }
        val transitioned = dataSource.connection.use { state(it, fixture) }
        require(transitioned.id == acceptedState.id) { "compatibility transition changed Runtime list-item id" }
        require(transitioned.sourceType == "ARTICLE" && transitioned.articleId != null) { "compatibility transition did not resolve current ARTICLE target: $transitioned" }
        require(transitioned.imagePath == null && transitioned.imageResourceId != null) { "compatibility transition did not move image to managed Resource: $transitioned" }
        require(transitioned.sourceFingerprint == fixture.currentFingerprint) { "compatibility transition mapping fingerprint is not current canonical state" }

        val second = facade.importCarousel(fixture.currentRoot)
        require(second.total == 1 && second.updated == 0 && second.skipped == 1 && second.conflicts == 0 && second.invalid == 0) {
            "transitioned item did not become Generic SKIP: $second"
        }
        require(dataSource.connection.use { state(it, fixture) } == transitioned) { "post-transition Generic SKIP changed Runtime state" }
    } else {
        require(current.total == 1 && current.updated == 0 && current.conflicts == 1 && current.invalid == 0) {
            "$scenario must be conflict/no-update: $current"
        }
        val conflict = current.results.singleOrNull { it.status == PartyCarouselPlacementImportStatus.CONFLICT }
        require(conflict != null) { "$scenario did not expose legacy carousel CONFLICT result: $current" }
        val after = dataSource.connection.use { state(it, fixture) }
        require(after == guardedBefore) { "$scenario mutated Runtime despite compatibility guard conflict: before=$guardedBefore after=$after" }
        require(after.sourceFingerprint == fixture.oldFingerprint) { "$scenario changed mapping fingerprint despite conflict" }
    }
}

private fun writeFixture(root: Path, scenario: CompatibilityScenario, objectMapper: ObjectMapper): Fixture {
    if (Files.exists(root)) root.toFile().deleteRecursively()
    val suffix = scenario.name.lowercase().replace('_', '-')
    val sourceSystem = "eu48-$suffix"
    val listCode = "EU48_${scenario.name}"
    val alias = "eu48-$suffix"
    val listLegacyKey = "carousel:$suffix"
    val articleLegacyKey = "article:$suffix"
    val oldFingerprint = digest("old-$suffix".toByteArray(StandardCharsets.UTF_8))
    val currentFingerprint = digest("current-$suffix".toByteArray(StandardCharsets.UTF_8))
    val articleFingerprint = digest("article-$suffix".toByteArray(StandardCharsets.UTF_8))
    val imageBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3, scenario.ordinal.toByte())
    val imageSha = digest(imageBytes)
    val sourcePage = "https://verification.invalid/$suffix/list"
    val sourceUrl = "https://verification.invalid/$suffix/item"
    val image = PartyCarouselSnapshotImage(
        sourceUrl = "https://verification.invalid/$suffix/card.png",
        originalReference = "card.png",
        snapshotPath = "assets/card.png",
        sha256 = imageSha,
        contentType = "image/png",
        sizeBytes = imageBytes.size.toLong(),
    )
    val evidence = PartyCarouselSnapshotEvidence(sourceUrl, "card.png", "_blank")
    val manifest = PartyManifest(sourceSystem, listOf(PartyContentScope("fixture", alias, 1)))

    val oldRoot = root.resolve("old")
    val currentRoot = root.resolve("current")
    writeManifest(oldRoot, manifest, objectMapper)
    writeManifest(currentRoot, manifest, objectMapper)

    val oldItem = linkedMapOf<String, Any?>(
        "legacyKey" to listLegacyKey,
        "sourceOrder" to 1,
        "title" to "EU-48 $suffix",
        "url" to sourceUrl,
        "openMode" to "NEW_WINDOW",
        "sourceFingerprint" to oldFingerprint,
        "image" to image,
        "evidence" to evidence,
    )
    writeList(
        oldRoot,
        listCode,
        sourceSystem,
        sourcePage,
        listLegacyKey,
        oldFingerprint,
        oldItem,
        imageBytes,
        objectMapper,
    )

    val currentItem = PartyCarouselPlacementItem(
        legacyKey = listLegacyKey,
        sourceOrder = 1,
        sourceType = CmsListItemSourceType.ARTICLE,
        title = "EU-48 $suffix",
        url = sourceUrl,
        articleRef = PartyCarouselArticleRef(sourceSystem, articleLegacyKey),
        openMode = "NEW_WINDOW",
        sourceFingerprint = currentFingerprint,
        image = image,
        evidence = evidence,
    )
    writeList(
        currentRoot,
        listCode,
        sourceSystem,
        sourcePage,
        listLegacyKey,
        currentFingerprint,
        currentItem,
        imageBytes,
        objectMapper,
    )

    val article = PartyMigrationRecord(
        source = PartyMigrationSource(
            system = sourceSystem,
            legacyKey = articleLegacyKey,
            contentId = scenario.ordinal.toString(),
            typeCode = "fixture",
            detailPath = "/$suffix/article",
            url = "https://verification.invalid/$suffix/article",
        ),
        target = PartyMigrationTarget(alias, ArticleType.EXTERNAL_LINK),
        content = PartyMigrationContent(
            title = "EU-48 $suffix",
            source = "EU-48 Verification",
            publishDate = null,
            bodyHtml = "",
            externalUrl = "https://example.com/$suffix",
        ),
        resources = emptyList(),
        sourceFingerprint = articleFingerprint,
        evidence = PartyMigrationEvidence(1, "EU-48 Verification", null, 1),
    )
    val articleFile = currentRoot.resolve("articles/item/article.json")
    Files.createDirectories(articleFile.parent)
    Files.writeString(articleFile, objectMapper.writeValueAsString(article))
    Files.writeString(
        currentRoot.resolve("index.ndjson"),
        objectMapper.writeValueAsString(
            PartyCanonicalIndexEntry(
                legacyKey = articleLegacyKey,
                path = "articles/item/article.json",
                sourceFingerprint = articleFingerprint,
                columnAlias = alias,
                articleType = ArticleType.EXTERNAL_LINK.name,
            ),
        ) + "\n",
    )

    if (scenario != CompatibilityScenario.MISSING_TRANSITION) {
        val transitionFingerprint = if (scenario == CompatibilityScenario.WRONG_FROM_FINGERPRINT) {
            digest("wrong-$suffix".toByteArray(StandardCharsets.UTF_8))
        } else {
            oldFingerprint
        }
        val authority = PartyCompatibilityAuthority(
            compatibilityVersion = 1,
            transitions = listOf(
                PartyCompatibilityTransition(
                    kind = "LIST_ITEM",
                    listCode = listCode,
                    sourceSystem = sourceSystem,
                    legacyKey = listLegacyKey,
                    fromFingerprint = transitionFingerprint,
                    fromSourceType = CmsListItemSourceType.LINK.name,
                    preserveRuntimeId = true,
                ),
            ),
        )
        Files.writeString(currentRoot.resolve("compatibility.json"), objectMapper.writeValueAsString(authority))
    }

    return Fixture(oldRoot, currentRoot, sourceSystem, listCode, listLegacyKey, oldFingerprint, currentFingerprint, articleLegacyKey)
}

private fun writeManifest(root: Path, manifest: PartyManifest, objectMapper: ObjectMapper) {
    Files.createDirectories(root)
    Files.writeString(root.resolve("manifest.json"), objectMapper.writeValueAsString(manifest))
}

private fun writeList(
    root: Path,
    listCode: String,
    sourceSystem: String,
    sourcePage: String,
    legacyKey: String,
    fingerprint: String,
    item: Any,
    imageBytes: ByteArray,
    objectMapper: ObjectMapper,
) {
    val itemPath = "items/item/item.json"
    val listRoot = root.resolve("lists/$listCode")
    val itemFile = listRoot.resolve(itemPath)
    Files.createDirectories(itemFile.parent.resolve("assets"))
    Files.write(itemFile.parent.resolve("assets/card.png"), imageBytes)
    Files.writeString(itemFile, objectMapper.writeValueAsString(item))
    Files.writeString(
        listRoot.resolve("index.json"),
        objectMapper.writeValueAsString(
            PartyCarouselCanonicalIndex(
                listCode = listCode,
                sourceSystem = sourceSystem,
                sourcePage = sourcePage,
                items = listOf(PartyCarouselCanonicalIndexItem(legacyKey, itemPath, 1, fingerprint)),
            ),
        ),
    )
}

private fun seedTargets(connection: Connection, fixture: Fixture, scenario: CompatibilityScenario) {
    val suffix = scenario.name.lowercase().replace('_', '-')
    val alias = "eu48-$suffix"
    connection.createStatement().use { statement ->
        statement.executeUpdate(
            "INSERT INTO cms_column(alias,name,cover_policy,sort_order,enabled,preset) VALUES ('$alias','EU48 $suffix','OPTIONAL',0,1,0)",
        )
        statement.executeUpdate(
            "INSERT INTO cms_list(code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset) VALUES ('${fixture.listCode}','EU48 $suffix','GENERAL','OPTIONAL','',0,1,0,0)",
        )
    }
}

private fun state(connection: Connection, fixture: Fixture): RuntimeListState = connection.prepareStatement(
    """
    SELECT i.id,i.source_type,i.article_id,i.url,i.image_path,i.image_resource_id,i.sort_order,m.source_fingerprint
    FROM cms_list_item i
    JOIN cms_list l ON l.id=i.list_id
    JOIN cms_list_item_legacy_mapping m ON m.list_item_id=i.id
    WHERE l.code=? AND m.source_system=? AND m.legacy_key=?
    """.trimIndent(),
).use { statement ->
    statement.setString(1, fixture.listCode)
    statement.setString(2, fixture.sourceSystem)
    statement.setString(3, fixture.listLegacyKey)
    statement.executeQuery().use { result ->
        require(result.next()) { "Runtime fixture item missing: ${fixture.listLegacyKey}" }
        RuntimeListState(
            id = result.getLong("id"),
            sourceType = result.getString("source_type"),
            articleId = result.getLong("article_id").takeUnless { result.wasNull() },
            url = result.getString("url"),
            imagePath = result.getString("image_path"),
            imageResourceId = result.getLong("image_resource_id").takeUnless { result.wasNull() },
            sortOrder = result.getInt("sort_order"),
            sourceFingerprint = result.getString("source_fingerprint"),
        )
    }
}

private fun updateItem(connection: Connection, fixture: Fixture, assignment: String) {
    connection.createStatement().use { statement ->
        val changed = statement.executeUpdate(
            "UPDATE cms_list_item SET $assignment WHERE id=(SELECT list_item_id FROM cms_list_item_legacy_mapping WHERE source_system='${fixture.sourceSystem}' AND legacy_key='${fixture.listLegacyKey}')",
        )
        require(changed == 1) { "Runtime fixture tamper did not affect exactly one item" }
    }
}

private fun digest(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(bytes)
    .joinToString("") { "%02x".format(it) }
