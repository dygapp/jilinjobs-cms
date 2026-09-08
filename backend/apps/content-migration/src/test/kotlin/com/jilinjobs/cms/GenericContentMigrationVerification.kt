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
import com.jilinjobs.cms.migration.generic.CanonicalListImage
import com.jilinjobs.cms.migration.generic.CanonicalListIndex
import com.jilinjobs.cms.migration.generic.CanonicalListItemRecord
import com.jilinjobs.cms.migration.generic.CanonicalListItemReference
import com.jilinjobs.cms.migration.generic.CanonicalMigrationResource
import com.jilinjobs.cms.migration.generic.GenericContentMigrationService
import com.jilinjobs.cms.migration.generic.GenericMigrationPhase
import com.jilinjobs.cms.migration.generic.main as runGenericCli
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.Connection
import javax.sql.DataSource
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import tools.jackson.databind.ObjectMapper

private enum class FixtureMode {
    VALID,
    CONFLICT,
    PATH_TRAVERSAL,
    MISSING_FILE,
    TAMPERED_RESOURCE,
    UNRESOLVED_REFERENCE,
    MISSING_TARGET,
    MISSING_DEPENDENCY,
    DUPLICATE_IDENTITY,
    INDEX_ITEM_MISMATCH,
}

fun main() {
    val dbUrl = requireNotNull(System.getenv("GENERIC_MIGRATION_VERIFY_DB_URL")) {
        "GENERIC_MIGRATION_VERIFY_DB_URL is required"
    }
    val workspace = Files.createTempDirectory("generic-content-migration-verify-")
    val storageRoot = workspace.resolve("uploads")
    val staticRoot = workspace.resolve("static")
    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=${System.getenv("GENERIC_MIGRATION_VERIFY_DB_USERNAME") ?: "root"}",
            "--spring.datasource.password=${System.getenv("GENERIC_MIGRATION_VERIFY_DB_PASSWORD") ?: "root"}",
            "--cms.storage.root=$storageRoot",
            "--cms.static.root=$staticRoot",
            "--spring.main.banner-mode=off",
        )
    try {
        val dataSource = context.getBean(DataSource::class.java)
        val objectMapper = context.getBean(ObjectMapper::class.java)
        val service = context.getBean(GenericContentMigrationService::class.java)
        dataSource.connection.use(::seedTargets)

        val validRoot = workspace.resolve("valid")
        writeFixture(validRoot, FixtureMode.VALID, objectMapper)
        val first = service.importSnapshot(validRoot)
        require(first.phase == GenericMigrationPhase.EXECUTE && first.total == 4 && first.created == 4 && first.conflicts == 0 && first.invalid == 0) {
            "First generic import result unexpected: $first"
        }
        val afterFirst = dataSource.connection.use(::snapshotCounts)
        require(afterFirst == RuntimeCounts(2, 2, 2, 2)) { "First import Runtime counts unexpected: $afterFirst" }

        val second = service.importSnapshot(validRoot)
        require(second.phase == GenericMigrationPhase.EXECUTE && second.total == 4 && second.created == 0 && second.skipped == 4 && second.conflicts == 0 && second.invalid == 0) {
            "Second generic import must be fully idempotent: $second"
        }
        require(dataSource.connection.use(::snapshotCounts) == afterFirst) { "Second import changed Runtime counts" }

        writeFixture(validRoot, FixtureMode.CONFLICT, objectMapper)
        val output = ByteArrayOutputStream()
        val originalOut = System.out
        var cliFailure: Throwable? = null
        try {
            System.setOut(PrintStream(output, true, StandardCharsets.UTF_8))
            cliFailure = runCatching {
                runGenericCli(
                    arrayOf(
                        validRoot.toString(),
                        "--spring.datasource.url=$dbUrl",
                        "--spring.datasource.username=${System.getenv("GENERIC_MIGRATION_VERIFY_DB_USERNAME") ?: "root"}",
                        "--spring.datasource.password=${System.getenv("GENERIC_MIGRATION_VERIFY_DB_PASSWORD") ?: "root"}",
                        "--cms.storage.root=$storageRoot",
                        "--cms.static.root=$staticRoot",
                        "--spring.main.banner-mode=off",
                    ),
                )
            }.exceptionOrNull()
        } finally {
            System.setOut(originalOut)
        }
        require(cliFailure != null) { "Fingerprint conflict must produce non-success CLI semantics" }
        require(output.toString(StandardCharsets.UTF_8).contains("CONTENT_MIGRATION_REPORT")) { "Generic CLI report label missing" }
        require(dataSource.connection.use(::snapshotCounts) == afterFirst) { "Conflict preflight mutated Runtime data" }

        val invalidModes = listOf(
            FixtureMode.PATH_TRAVERSAL,
            FixtureMode.MISSING_FILE,
            FixtureMode.TAMPERED_RESOURCE,
            FixtureMode.UNRESOLVED_REFERENCE,
            FixtureMode.MISSING_TARGET,
            FixtureMode.MISSING_DEPENDENCY,
            FixtureMode.DUPLICATE_IDENTITY,
            FixtureMode.INDEX_ITEM_MISMATCH,
        )
        invalidModes.forEach { mode ->
            val root = workspace.resolve(mode.name.lowercase())
            writeFixture(root, mode, objectMapper)
            val before = dataSource.connection.use(::snapshotCounts)
            val report = service.importSnapshot(root)
            require(report.phase == GenericMigrationPhase.PREFLIGHT && report.invalid + report.conflicts > 0) {
                "$mode must fail during preflight: $report"
            }
            require(dataSource.connection.use(::snapshotCounts) == before) { "$mode mutated Runtime data before preflight closed" }
        }

        require(runCatching { Class.forName("org.springframework.web.servlet.DispatcherServlet") }.isFailure) {
            "Generic verification classpath contains Spring MVC DispatcherServlet"
        }
        println("GENERIC_CONTENT_MIGRATION_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun seedTargets(connection: Connection) {
    connection.createStatement().use { statement ->
        statement.executeUpdate(
            """
            INSERT INTO cms_column(alias,name,cover_policy,sort_order,enabled,preset)
            VALUES ('verification-news','Verification News','OPTIONAL',0,1,0)
            """.trimIndent(),
        )
        statement.executeUpdate(
            """
            INSERT INTO cms_list(code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset)
            VALUES ('VERIFY_FEATURED','Verification Featured','GENERAL','OPTIONAL','',0,1,0,0)
            """.trimIndent(),
        )
    }
}

private data class RuntimeCounts(
    val articles: Int,
    val articleMappings: Int,
    val listItems: Int,
    val listMappings: Int,
)

private fun snapshotCounts(connection: Connection) = RuntimeCounts(
    count(connection, "cms_article"),
    count(connection, "cms_article_legacy_mapping"),
    count(connection, "cms_list_item"),
    count(connection, "cms_list_item_legacy_mapping"),
)

private fun count(connection: Connection, table: String): Int =
    connection.createStatement().use { statement ->
        statement.executeQuery("SELECT COUNT(*) FROM $table").use { result ->
            require(result.next())
            result.getInt(1)
        }
    }

private fun writeFixture(root: Path, mode: FixtureMode, objectMapper: ObjectMapper) {
    if (Files.exists(root)) root.toFile().deleteRecursively()
    Files.createDirectories(root)
    val sourceSystem = if (mode in setOf(FixtureMode.VALID, FixtureMode.CONFLICT)) "verification-source" else "verification-${mode.name.lowercase()}"
    val png = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3, 4)
    val attachment = "verification attachment".toByteArray(StandardCharsets.UTF_8)
    val pngSha = digest(png)
    val attachmentSha = digest(attachment)
    val articleFingerprint = digest("$sourceSystem-internal-${if (mode == FixtureMode.CONFLICT) "v2" else "v1"}".toByteArray())
    val externalFingerprint = digest("$sourceSystem-external-v1".toByteArray())

    val bodyPath = "assets/body.png"
    val attachmentPath = "assets/info.txt"
    var internal = CanonicalArticleRecord(
        source = CanonicalArticleSource(sourceSystem, "article:internal", "1", "news", "/legacy/internal", "https://verification.invalid/internal"),
        target = CanonicalArticleTarget(if (mode == FixtureMode.MISSING_TARGET) "verification-missing" else "verification-news", ArticleType.INTERNAL),
        content = CanonicalArticleContent(
            "Verification Internal",
            "Verification",
            null,
            "<p><img src=\"migration-resource://$pngSha\"></p><a href=\"migration-attachment://$attachmentSha\">file</a>",
            null,
        ),
        resources = listOf(
            CanonicalMigrationResource("BODY_IMAGE", "https://verification.invalid/body.png", "body.png", "migration-resource://$pngSha", bodyPath, pngSha, "image/png", png.size.toLong()),
            CanonicalMigrationResource("ATTACHMENT", "https://verification.invalid/info.txt", "info.txt", "migration-attachment://$attachmentSha", attachmentPath, attachmentSha, "text/plain", attachment.size.toLong()),
        ),
        sourceFingerprint = articleFingerprint,
        evidence = CanonicalArticleEvidence(1, "Verification", null, 1),
    )
    if (mode == FixtureMode.MISSING_FILE) {
        internal = internal.copy(resources = internal.resources.mapIndexed { index, item -> if (index == 0) item.copy(snapshotPath = "assets/missing.png") else item })
    }
    if (mode == FixtureMode.UNRESOLVED_REFERENCE) {
        val unknown = digest("unknown-resource".toByteArray())
        internal = internal.copy(content = internal.content.copy(bodyHtml = internal.content.bodyHtml + "<img src=\"migration-resource://$unknown\">"))
    }

    val external = CanonicalArticleRecord(
        source = CanonicalArticleSource(sourceSystem, "article:external", "2", "news", "/legacy/external", "https://verification.invalid/external"),
        target = CanonicalArticleTarget("verification-news", ArticleType.EXTERNAL_LINK),
        content = CanonicalArticleContent("Verification External", "Verification", null, "", "https://example.com/verification"),
        resources = emptyList(),
        sourceFingerprint = externalFingerprint,
        evidence = CanonicalArticleEvidence(1, "Verification", null, 2),
    )

    val internalFile = root.resolve("articles/internal/article.json")
    val externalFile = root.resolve("articles/external/article.json")
    Files.createDirectories(internalFile.parent.resolve("assets"))
    Files.createDirectories(externalFile.parent)
    val bodyBytes = if (mode == FixtureMode.TAMPERED_RESOURCE) png.copyOf().also { it[it.lastIndex] = (it.last() + 1).toByte() } else png
    if (mode != FixtureMode.MISSING_FILE) Files.write(internalFile.parent.resolve(bodyPath), bodyBytes)
    Files.write(internalFile.parent.resolve(attachmentPath), attachment)
    Files.writeString(internalFile, objectMapper.writeValueAsString(internal))
    Files.writeString(externalFile, objectMapper.writeValueAsString(external))

    val articleEntries = mutableListOf(
        CanonicalArticleIndexEntry("article:internal", if (mode == FixtureMode.PATH_TRAVERSAL) "../outside.json" else "articles/internal/article.json"),
        CanonicalArticleIndexEntry("article:external", "articles/external/article.json"),
    )
    if (mode == FixtureMode.DUPLICATE_IDENTITY) articleEntries += CanonicalArticleIndexEntry("article:internal", "articles/internal/article.json")
    Files.writeString(root.resolve("index.ndjson"), articleEntries.joinToString("\n") { objectMapper.writeValueAsString(it) } + "\n")

    val listRoot = root.resolve("lists/VERIFY_FEATURED")
    val linkFile = listRoot.resolve("items/link/item.json")
    val articleItemFile = listRoot.resolve("items/article/item.json")
    Files.createDirectories(linkFile.parent.resolve("assets"))
    Files.createDirectories(articleItemFile.parent)
    Files.write(linkFile.parent.resolve("assets/card.png"), png)
    val linkFingerprint = digest("$sourceSystem-link-v1".toByteArray())
    val articleItemFingerprint = digest("$sourceSystem-article-item-v1".toByteArray())
    val linkItem = CanonicalListItemRecord(
        legacyKey = "list:link",
        sourceOrder = 1,
        sourceType = CmsListItemSourceType.LINK,
        title = "Verification Link",
        url = "https://example.com/link",
        sourceFingerprint = linkFingerprint,
        image = CanonicalListImage("https://verification.invalid/card.png", "assets/card.png", pngSha, "image/png", png.size.toLong()),
    )
    val articleReference = if (mode == FixtureMode.MISSING_DEPENDENCY) CanonicalArticleReference("unmapped-source", "article:missing") else CanonicalArticleReference(sourceSystem, "article:internal")
    val articleItem = CanonicalListItemRecord(
        legacyKey = "list:article",
        sourceOrder = 2,
        sourceType = CmsListItemSourceType.ARTICLE,
        title = "Verification Article",
        articleReference = articleReference,
        sourceFingerprint = articleItemFingerprint,
    )
    Files.writeString(linkFile, objectMapper.writeValueAsString(linkItem))
    Files.writeString(articleItemFile, objectMapper.writeValueAsString(articleItem))
    val linkReferenceFingerprint = if (mode == FixtureMode.INDEX_ITEM_MISMATCH) digest("mismatch".toByteArray()) else linkFingerprint
    val listIndex = CanonicalListIndex(
        listCode = "VERIFY_FEATURED",
        sourceSystem = sourceSystem,
        sourcePage = "https://verification.invalid/list",
        items = listOf(
            CanonicalListItemReference("list:link", "items/link/item.json", 1, linkReferenceFingerprint),
            CanonicalListItemReference("list:article", "items/article/item.json", 2, articleItemFingerprint),
        ),
    )
    Files.writeString(listRoot.resolve("index.json"), objectMapper.writeValueAsString(listIndex))
}

private fun digest(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
