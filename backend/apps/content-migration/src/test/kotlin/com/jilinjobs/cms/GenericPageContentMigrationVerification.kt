package com.jilinjobs.cms

import com.jilinjobs.cms.migration.generic.CanonicalPageContent
import com.jilinjobs.cms.migration.generic.CanonicalPageIndex
import com.jilinjobs.cms.migration.generic.CanonicalPageRecord
import com.jilinjobs.cms.migration.generic.CanonicalPageReference
import com.jilinjobs.cms.migration.generic.CanonicalPageResource
import com.jilinjobs.cms.migration.generic.CanonicalPageSource
import com.jilinjobs.cms.migration.generic.CanonicalPageTarget
import com.jilinjobs.cms.migration.generic.GenericContentMigrationService
import com.jilinjobs.cms.migration.generic.GenericMigrationKind
import com.jilinjobs.cms.migration.generic.GenericMigrationPhase
import com.jilinjobs.cms.migration.generic.GenericMigrationStatus
import com.jilinjobs.cms.migration.generic.pageContentFingerprint
import com.jilinjobs.cms.migration.generic.main as runGenericCli
import com.jilinjobs.cms.page.PageRenderMode
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

private enum class PageFixtureMode {
    VALID,
    SOURCE_FINGERPRINT_CONFLICT,
    WRONG_TARGET_PRECONDITION,
    MISSING_TARGET,
    TAMPERED_RESOURCE,
    PATH_TRAVERSAL,
    STATIC_TARGET_CONFLICT,
}

fun main() {
    val dbUrl = requireNotNull(System.getenv("GENERIC_MIGRATION_VERIFY_DB_URL")) {
        "GENERIC_MIGRATION_VERIFY_DB_URL is required"
    }
    val workspace = Files.createTempDirectory("generic-page-migration-verify-")
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
        dataSource.connection.use(::seedPageTargets)

        val validRoot = workspace.resolve("valid")
        writePageFixture(validRoot, PageFixtureMode.VALID, objectMapper)
        val first = service.importSnapshot(validRoot)
        require(first.phase == GenericMigrationPhase.EXECUTE && first.total == 2 && first.created == 2 && first.conflicts == 0 && first.invalid == 0) {
            "First Page import result unexpected: $first"
        }
        require(first.results.all { it.kind == GenericMigrationKind.PAGE && it.status == GenericMigrationStatus.CREATED }) {
            "First Page import must report PAGE/CREATED results: $first"
        }
        require(dataSource.connection.use { count(it, "cms_page_legacy_mapping") } == 2) { "Page mapping count unexpected after first apply" }
        verifyAppliedPages(dataSource, staticRoot)

        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE cms_page SET body_html=? WHERE group_id IS NULL AND alias='about'").use { statement ->
                statement.setString(1, "<p>Operator edit after migration</p>")
                require(statement.executeUpdate() == 1)
            }
        }
        val second = service.importSnapshot(validRoot)
        require(second.phase == GenericMigrationPhase.EXECUTE && second.total == 2 && second.created == 0 && second.skipped == 2 && second.conflicts == 0 && second.invalid == 0) {
            "Second Page import must be fully idempotent: $second"
        }
        require(pageBody(dataSource, null, "about") == "<p>Operator edit after migration</p>") {
            "Same-source Page SKIP overwrote operator edit"
        }

        writePageFixture(validRoot, PageFixtureMode.SOURCE_FINGERPRINT_CONFLICT, objectMapper)
        val beforeSourceConflict = snapshotPageState(dataSource)
        val sourceConflict = service.importSnapshot(validRoot)
        require(sourceConflict.phase == GenericMigrationPhase.PREFLIGHT && sourceConflict.conflicts > 0) {
            "Changed Page source fingerprint must conflict before execute: $sourceConflict"
        }
        require(snapshotPageState(dataSource) == beforeSourceConflict) { "Source fingerprint conflict mutated Page Runtime" }

        writePageFixture(validRoot, PageFixtureMode.VALID, objectMapper)
        dataSource.connection.use { connection ->
            val guideId = pageId(connection, "docs", "guide")
            connection.prepareStatement("UPDATE cms_page_legacy_mapping SET page_id=? WHERE source_system='page-verification' AND legacy_key='page:about'").use { statement ->
                statement.setLong(1, guideId)
                require(statement.executeUpdate() == 1)
            }
        }
        val beforeMappingConflict = snapshotPageState(dataSource)
        val mappingConflict = service.importSnapshot(validRoot)
        require(mappingConflict.phase == GenericMigrationPhase.PREFLIGHT && mappingConflict.conflicts > 0) {
            "Mapping target drift must conflict before execute: $mappingConflict"
        }
        require(snapshotPageState(dataSource) == beforeMappingConflict) { "Mapping target drift mutated Page Runtime" }
        dataSource.connection.use { connection ->
            val aboutId = pageId(connection, null, "about")
            connection.prepareStatement("UPDATE cms_page_legacy_mapping SET page_id=? WHERE source_system='page-verification' AND legacy_key='page:about'").use { statement ->
                statement.setLong(1, aboutId)
                require(statement.executeUpdate() == 1)
            }
        }

        val guardModes = listOf(
            PageFixtureMode.WRONG_TARGET_PRECONDITION,
            PageFixtureMode.MISSING_TARGET,
            PageFixtureMode.TAMPERED_RESOURCE,
            PageFixtureMode.PATH_TRAVERSAL,
            PageFixtureMode.STATIC_TARGET_CONFLICT,
        )
        guardModes.forEach { mode ->
            val root = workspace.resolve(mode.name.lowercase())
            writePageFixture(root, mode, objectMapper)
            if (mode == PageFixtureMode.STATIC_TARGET_CONFLICT) {
                installConflictingStaticTarget(root, staticRoot, objectMapper)
            }
            val before = snapshotPageState(dataSource)
            val report = service.importSnapshot(root)
            require(report.phase == GenericMigrationPhase.PREFLIGHT && report.invalid + report.conflicts > 0) {
                "$mode must fail during Page preflight: $report"
            }
            require(snapshotPageState(dataSource) == before) { "$mode mutated Page Runtime before preflight closed" }
        }

        writePageFixture(validRoot, PageFixtureMode.VALID, objectMapper)
        val cliOutput = ByteArrayOutputStream()
        val originalOut = System.out
        try {
            System.setOut(PrintStream(cliOutput, true, StandardCharsets.UTF_8))
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
        } finally {
            System.setOut(originalOut)
        }
        val output = cliOutput.toString(StandardCharsets.UTF_8)
        require(output.contains("CONTENT_MIGRATION_REPORT") && output.contains("PAGE")) {
            "Generic CLI Page report is missing PAGE kind: $output"
        }
        require(pageBody(dataSource, null, "about") == "<p>Operator edit after migration</p>") {
            "Generic CLI same-source Page run overwrote operator edit"
        }

        println("GENERIC_PAGE_CONTENT_MIGRATION_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun seedPageTargets(connection: Connection) {
    connection.createStatement().use { statement ->
        statement.executeUpdate(
            """
            INSERT INTO cms_page_group(alias,name,sort_order,enabled,preset)
            VALUES ('docs','Verification Docs',0,1,1)
            """.trimIndent(),
        )
        statement.executeUpdate(
            """
            INSERT INTO cms_page(group_id,alias,name,body_html,content_model,renderer_key,content_owner,structured_payload,embed_url,sort_order,enabled,preset)
            VALUES (NULL,'about','Verification About','<p>About placeholder</p>','RICH_TEXT','RICH_TEXT','OPERATOR',NULL,NULL,0,1,1)
            """.trimIndent(),
        )
        statement.executeUpdate(
            """
            INSERT INTO cms_page(group_id,alias,name,body_html,content_model,renderer_key,content_owner,structured_payload,embed_url,sort_order,enabled,preset)
            SELECT id,'guide','Verification Guide','<p>Guide placeholder</p>','RICH_TEXT','RICH_TEXT','OPERATOR',NULL,NULL,0,1,1
            FROM cms_page_group WHERE alias='docs'
            """.trimIndent(),
        )
        statement.executeUpdate(
            """
            INSERT INTO cms_page(group_id,alias,name,body_html,content_model,renderer_key,content_owner,structured_payload,embed_url,sort_order,enabled,preset)
            VALUES (NULL,'guard','Verification Guard','<p>Guard placeholder</p>','RICH_TEXT','RICH_TEXT','OPERATOR',NULL,NULL,0,1,1)
            """.trimIndent(),
        )
    }
}

private data class PageRuntimeState(
    val aboutBody: String,
    val guideBody: String,
    val guardBody: String,
    val mappings: Int,
)

private fun snapshotPageState(dataSource: DataSource): PageRuntimeState = dataSource.connection.use { connection ->
    PageRuntimeState(
        pageBody(connection, null, "about"),
        pageBody(connection, "docs", "guide"),
        pageBody(connection, null, "guard"),
        count(connection, "cms_page_legacy_mapping"),
    )
}

private fun verifyAppliedPages(dataSource: DataSource, staticRoot: Path) {
    val about = pageBody(dataSource, null, "about")
    require(!about.contains("<script", ignoreCase = true)) { "PageService sanitizer did not remove script content" }
    require(about.contains("/static/migrated/content/pages/")) { "Page resource token was not rewritten: $about" }
    require(pageBody(dataSource, "docs", "guide").contains("Guide canonical")) { "Grouped Page was not applied" }
    require(Files.walk(staticRoot.resolve("migrated/content/pages")).use { stream -> stream.anyMatch(Files::isRegularFile) }) {
        "Page deterministic static resource was not projected"
    }
}

private fun writePageFixture(root: Path, mode: PageFixtureMode, objectMapper: ObjectMapper) {
    if (Files.exists(root)) root.toFile().deleteRecursively()
    Files.createDirectories(root.resolve("pages/items/about/assets"))
    Files.createDirectories(root.resolve("pages/items/guide"))
    Files.createDirectories(root.resolve("pages/items/guard/assets"))

    val sourceSystem = when (mode) {
        PageFixtureMode.VALID, PageFixtureMode.SOURCE_FINGERPRINT_CONFLICT -> "page-verification"
        else -> "page-${mode.name.lowercase()}"
    }
    val png = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3, 4)
    val pngSha = digest(png)
    val aboutFingerprint = digest("$sourceSystem-about-${if (mode == PageFixtureMode.SOURCE_FINGERPRINT_CONFLICT) "v2" else "v1"}".toByteArray())
    val guideFingerprint = digest("$sourceSystem-guide-v1".toByteArray())
    val guardFingerprint = digest("$sourceSystem-guard-v1".toByteArray())
    val aboutPrecondition = pageContentFingerprint("<p>About placeholder</p>", PageRenderMode.RICH_TEXT, null, objectMapper)
    val guidePrecondition = pageContentFingerprint("<p>Guide placeholder</p>", PageRenderMode.RICH_TEXT, null, objectMapper)
    val guardPrecondition = pageContentFingerprint("<p>Guard placeholder</p>", PageRenderMode.RICH_TEXT, null, objectMapper)

    val about = CanonicalPageRecord(
        source = CanonicalPageSource(sourceSystem, "page:about", "https://verification.invalid/pages/about"),
        target = CanonicalPageTarget(pageAlias = "about"),
        content = CanonicalPageContent(
            bodyHtml = "<script>alert('remove')</script><p><img src=\"migration-resource://$pngSha\"></p>",
            renderMode = PageRenderMode.RICH_TEXT,
        ),
        resources = listOf(
            CanonicalPageResource(
                sourceUrl = "https://verification.invalid/assets/about.png",
                snapshotPath = "assets/about.png",
                sha256 = pngSha,
                contentType = "image/png",
                sizeBytes = png.size.toLong(),
                token = "migration-resource://$pngSha",
            ),
        ),
        sourceFingerprint = aboutFingerprint,
        expectedTargetFingerprint = aboutPrecondition,
    )
    val guide = CanonicalPageRecord(
        source = CanonicalPageSource(sourceSystem, "page:guide", "https://verification.invalid/pages/guide"),
        target = CanonicalPageTarget("docs", "guide"),
        content = CanonicalPageContent("<p>Guide canonical</p>", PageRenderMode.RICH_TEXT),
        sourceFingerprint = guideFingerprint,
        expectedTargetFingerprint = guidePrecondition,
    )
    val guard = CanonicalPageRecord(
        source = CanonicalPageSource(sourceSystem, "page:guard", "https://verification.invalid/pages/guard"),
        target = CanonicalPageTarget(pageAlias = if (mode == PageFixtureMode.MISSING_TARGET) "missing-page" else "guard"),
        content = CanonicalPageContent(
            bodyHtml = "<p><img src=\"migration-resource://$pngSha\"></p>",
            renderMode = PageRenderMode.RICH_TEXT,
        ),
        resources = listOf(
            CanonicalPageResource(
                sourceUrl = "https://verification.invalid/assets/guard.png",
                snapshotPath = "assets/guard.png",
                sha256 = pngSha,
                contentType = "image/png",
                sizeBytes = png.size.toLong(),
                token = "migration-resource://$pngSha",
            ),
        ),
        sourceFingerprint = guardFingerprint,
        expectedTargetFingerprint = if (mode == PageFixtureMode.WRONG_TARGET_PRECONDITION) digest("wrong-target".toByteArray()) else guardPrecondition,
    )

    val items = when (mode) {
        PageFixtureMode.VALID, PageFixtureMode.SOURCE_FINGERPRINT_CONFLICT -> listOf(
            Triple("page:about", "items/about/page.json", about),
            Triple("page:guide", "items/guide/page.json", guide),
        )
        else -> listOf(Triple("page:guard", "items/guard/page.json", guard))
    }

    Files.write(root.resolve("pages/items/about/assets/about.png"), png)
    Files.write(root.resolve("pages/items/guard/assets/guard.png"), if (mode == PageFixtureMode.TAMPERED_RESOURCE) png.copyOf().also { it[it.lastIndex] = 9 } else png)
    Files.writeString(root.resolve("pages/items/about/page.json"), objectMapper.writeValueAsString(about))
    Files.writeString(root.resolve("pages/items/guide/page.json"), objectMapper.writeValueAsString(guide))
    Files.writeString(root.resolve("pages/items/guard/page.json"), objectMapper.writeValueAsString(guard))

    if (mode == PageFixtureMode.PATH_TRAVERSAL) {
        Files.writeString(root.resolve("outside-page.json"), objectMapper.writeValueAsString(guard))
    }
    val references = items.mapIndexed { index, (legacyKey, path, record) ->
        CanonicalPageReference(
            legacyKey = legacyKey,
            path = if (mode == PageFixtureMode.PATH_TRAVERSAL && index == 0) "../outside-page.json" else path,
            sourceOrder = index + 1,
            sourceFingerprint = record.sourceFingerprint,
        )
    }
    val index = CanonicalPageIndex(sourceSystem, references)
    Files.writeString(root.resolve("pages/index.json"), objectMapper.writeValueAsString(index))
}

private fun installConflictingStaticTarget(root: Path, staticRoot: Path, objectMapper: ObjectMapper) {
    val index = objectMapper.readValue(root.resolve("pages/index.json").toFile(), CanonicalPageIndex::class.java)
    val reference = index.items.single()
    val record = objectMapper.readValue(root.resolve("pages").resolve(reference.path).toFile(), CanonicalPageRecord::class.java)
    val resource = record.resources.single()
    val target = staticRoot.resolve("migrated/content/pages/${resource.sha256}.png")
    Files.createDirectories(target.parent)
    Files.write(target, byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 9, 9, 9, 9))
}

private fun pageBody(dataSource: DataSource, groupAlias: String?, alias: String): String =
    dataSource.connection.use { pageBody(it, groupAlias, alias) }

private fun pageBody(connection: Connection, groupAlias: String?, alias: String): String {
    val sql = if (groupAlias == null) {
        "SELECT body_html FROM cms_page WHERE group_id IS NULL AND alias=?"
    } else {
        "SELECT p.body_html FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias=? AND p.alias=?"
    }
    return connection.prepareStatement(sql).use { statement ->
        if (groupAlias == null) statement.setString(1, alias) else {
            statement.setString(1, groupAlias)
            statement.setString(2, alias)
        }
        statement.executeQuery().use { result ->
            require(result.next()) { "Page target missing: ${groupAlias ?: "<root>"}/$alias" }
            result.getString(1)
        }
    }
}

private fun pageId(connection: Connection, groupAlias: String?, alias: String): Long {
    val sql = if (groupAlias == null) {
        "SELECT id FROM cms_page WHERE group_id IS NULL AND alias=?"
    } else {
        "SELECT p.id FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias=? AND p.alias=?"
    }
    return connection.prepareStatement(sql).use { statement ->
        if (groupAlias == null) statement.setString(1, alias) else {
            statement.setString(1, groupAlias)
            statement.setString(2, alias)
        }
        statement.executeQuery().use { result ->
            require(result.next())
            result.getLong(1)
        }
    }
}

private fun count(connection: Connection, table: String): Int =
    connection.createStatement().use { statement ->
        statement.executeQuery("SELECT COUNT(*) FROM $table").use { result ->
            require(result.next())
            result.getInt(1)
        }
    }

private fun digest(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
