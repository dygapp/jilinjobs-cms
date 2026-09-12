package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.page.PageStructuredContent
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import javax.sql.DataSource
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import tools.jackson.databind.ObjectMapper

private const val ADOPTION_TEST_ALIAS = "eu52-adoption-verification"
private const val STRUCTURED_TEST_ALIAS = "eu55-structured-adoption-verification"

fun main() {
    val dbUrl = requireNotNull(System.getenv("SITE_PACKAGE_VERIFY_DB_URL")) { "SITE_PACKAGE_VERIFY_DB_URL is required" }
    val context = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=${System.getenv("SITE_PACKAGE_VERIFY_DB_USERNAME") ?: "root"}",
            "--spring.datasource.password=${System.getenv("SITE_PACKAGE_VERIFY_DB_PASSWORD") ?: "root"}",
            "--spring.main.banner-mode=off",
        )
    try {
        val dataSource = context.getBean(DataSource::class.java)
        val provisioner = context.getBean(SitePackageProvisioner::class.java)
        val loader = context.getBean(SitePackageLoader::class.java)
        val objectMapper = context.getBean(ObjectMapper::class.java)
        verifyLegacyRichAdoption(dataSource, provisioner, loader)
        verifyStructuredAdoption(dataSource, provisioner, loader, objectMapper)
        println("EU52_EU55_PAGE_CONTENT_ADOPTION_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun verifyLegacyRichAdoption(dataSource: DataSource, provisioner: SitePackageProvisioner, loader: SitePackageLoader) {
    val root = Files.createTempDirectory("eu52-page-adoption-")
    val oldBody = "<p>关于我们内容可通过固定页面管理维护。</p>"
    val acceptedOldFingerprint = "7b0edda0a0731b89e578d442cdf9e243dccf7e97eaa8d867aeb068742733904e"
    val testIdentity = "<root>:$ADOPTION_TEST_ALIAS"

    deleteTestPage(dataSource, ADOPTION_TEST_ALIAS)
    writeLegacyPackage(root, ADOPTION_TEST_ALIAS, "About", oldBody, 10, null)
    val first = provisioner.apply(root)
    require(first.created == 1 && first.updated == 0 && first.unchanged == 0) { "Initial isolated create unexpected: $first" }
    require(first.adoptedPageContent.isEmpty() && first.protectedPageContent.isEmpty())
    require(readPage(dataSource, ADOPTION_TEST_ALIAS).matchesLegacy(oldBody, "RICH_TEXT", null))

    writeLegacyPackage(root, ADOPTION_TEST_ALIAS, "About", "<p>Formal package content</p>", 10, acceptedOldFingerprint)
    val adopted = provisioner.apply(root)
    require(adopted.created == 0 && adopted.updated == 1 && adopted.unchanged == 0) { "Adoption counters unexpected: $adopted" }
    require(adopted.adoptedPageContent == listOf(testIdentity)) { "Adoption identity report unexpected: $adopted" }
    require(adopted.protectedPageContent.isEmpty())
    require(readPage(dataSource, ADOPTION_TEST_ALIAS).matchesLegacy("<p>Formal package content</p>", "RICH_TEXT", null))

    val idempotent = provisioner.apply(root)
    require(idempotent.created == 0 && idempotent.updated == 0 && idempotent.unchanged == 1) { "Adoption rerun not idempotent: $idempotent" }
    require(idempotent.adoptedPageContent.isEmpty() && idempotent.protectedPageContent.isEmpty())

    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "UPDATE cms_page SET body_html=?,content_model=?,renderer_key=?,content_owner=?,structured_payload=?,embed_url=? WHERE group_id IS NULL AND alias=?",
        ).use {
            it.setString(1, "<p>Operator edit after adoption</p>")
            it.setString(2, "NONE")
            it.setString(3, "INTERNAL_STATIC")
            it.setString(4, "ENGINEERING")
            it.setString(5, null)
            it.setString(6, "/operator-owned")
            it.setString(7, ADOPTION_TEST_ALIAS)
            require(it.executeUpdate() == 1)
        }
    }
    val operator = readPage(dataSource, ADOPTION_TEST_ALIAS)
    val protected = provisioner.apply(root)
    require(protected.created == 0 && protected.updated == 0 && protected.unchanged == 1)
    require(protected.adoptedPageContent.isEmpty())
    require(protected.protectedPageContent == listOf(testIdentity)) { "Protected identity report missing: $protected" }
    require(readPage(dataSource, ADOPTION_TEST_ALIAS) == operator) { "Ordinary reconcile overwrote operator content" }

    writeLegacyPackage(root, ADOPTION_TEST_ALIAS, "About renamed", "<p>Formal package content</p>", 20, acceptedOldFingerprint)
    val structural = provisioner.apply(root)
    require(structural.created == 0 && structural.updated == 1 && structural.unchanged == 0)
    require(structural.protectedPageContent == listOf(testIdentity))
    require(readPage(dataSource, ADOPTION_TEST_ALIAS) == operator.copy(name = "About renamed", sortOrder = 20)) { "Structural reconcile did not preserve protected content" }

    writeLegacyPackage(root, ADOPTION_TEST_ALIAS, "Invalid", "<p>Invalid</p>", 10, "not-a-sha")
    val invalid = runCatching { loader.load(root) }.exceptionOrNull()
    require(invalid is SitePackageValidationException) { "Invalid adoption fingerprint must fail closed" }
}

private fun verifyStructuredAdoption(
    dataSource: DataSource,
    provisioner: SitePackageProvisioner,
    loader: SitePackageLoader,
    objectMapper: ObjectMapper,
) {
    val root = Files.createTempDirectory("eu55-structured-adoption-")
    val legacyBody = "<section><h2>第一项</h2><p>旧正文</p></section><section><h2>第二项</h2><p>旧正文二</p></section>"
    val acceptedLegacyFingerprint = legacyFingerprint(objectMapper, legacyBody, "RICH_TEXT", null)
    val identity = "<root>:$STRUCTURED_TEST_ALIAS"

    deleteTestPage(dataSource, STRUCTURED_TEST_ALIAS)
    writeStructuredPackage(root, "Structured Fresh", 10, null)
    val fresh = provisioner.apply(root)
    require(fresh.created == 1 && fresh.updated == 0 && fresh.unchanged == 0) { "Structured fresh create unexpected: $fresh" }
    assertStructuredState(readPage(dataSource, STRUCTURED_TEST_ALIAS), objectMapper)

    deleteTestPage(dataSource, STRUCTURED_TEST_ALIAS)
    writeLegacyPackage(root, STRUCTURED_TEST_ALIAS, "Legacy predecessor", legacyBody, 10, null)
    require(provisioner.apply(root).created == 1)

    writeStructuredPackage(root, "Structured target", 10, acceptedLegacyFingerprint)
    val adopted = provisioner.apply(root)
    require(adopted.created == 0 && adopted.updated == 1 && adopted.unchanged == 0) { "Structured adoption counters unexpected: $adopted" }
    require(adopted.adoptedPageContent == listOf(identity)) { "Structured adoption report missing: $adopted" }
    assertStructuredState(readPage(dataSource, STRUCTURED_TEST_ALIAS), objectMapper)

    val idempotent = provisioner.apply(root)
    require(idempotent.created == 0 && idempotent.updated == 0 && idempotent.unchanged == 1) { "Structured rerun must be idempotent: $idempotent" }
    require(idempotent.adoptedPageContent.isEmpty() && idempotent.protectedPageContent.isEmpty())

    val operatorPayload = """{"schemaVersion":1,"kind":"CARD_COLLECTION","items":[{"title":"Operator card","bodyHtml":"<p>Operator maintained</p>"}]}"""
    dataSource.connection.use { connection ->
        connection.prepareStatement("UPDATE cms_page SET structured_payload=? WHERE group_id IS NULL AND alias=?").use {
            it.setString(1, operatorPayload)
            it.setString(2, STRUCTURED_TEST_ALIAS)
            require(it.executeUpdate() == 1)
        }
    }
    val operator = readPage(dataSource, STRUCTURED_TEST_ALIAS)
    val protected = provisioner.apply(root)
    require(protected.created == 0 && protected.updated == 0 && protected.unchanged == 1)
    require(protected.protectedPageContent == listOf(identity)) { "Structured operator edit was not protected: $protected" }
    require(readPage(dataSource, STRUCTURED_TEST_ALIAS) == operator) { "Structured operator edit was overwritten" }

    writeStructuredPackage(root, "Structured renamed", 20, acceptedLegacyFingerprint)
    val structural = provisioner.apply(root)
    require(structural.created == 0 && structural.updated == 1 && structural.unchanged == 0)
    require(structural.protectedPageContent == listOf(identity))
    require(readPage(dataSource, STRUCTURED_TEST_ALIAS) == operator.copy(name = "Structured renamed", sortOrder = 20)) { "Structured structural reconcile overwrote operator payload" }

    deleteTestPage(dataSource, STRUCTURED_TEST_ALIAS)
    writeLegacyPackage(root, STRUCTURED_TEST_ALIAS, "Legacy unsafe", legacyBody, 10, null)
    require(provisioner.apply(root).created == 1)
    dataSource.connection.use { connection ->
        connection.prepareStatement("UPDATE cms_page SET content_owner='EXTERNAL' WHERE group_id IS NULL AND alias=?").use {
            it.setString(1, STRUCTURED_TEST_ALIAS)
            require(it.executeUpdate() == 1)
        }
    }
    writeStructuredPackage(root, "Structured target", 10, acceptedLegacyFingerprint)
    val unsafe = provisioner.apply(root)
    require(unsafe.adoptedPageContent.isEmpty()) { "Inconsistent orthogonal predecessor must not be adopted: $unsafe" }
    require(unsafe.protectedPageContent == listOf(identity)) { "Inconsistent predecessor must fail closed: $unsafe" }
    require(readPage(dataSource, STRUCTURED_TEST_ALIAS).contentOwner == "EXTERNAL")

    writeStructuredPackage(root, "Invalid v2", 10, acceptedLegacyFingerprint, includeLegacyRenderMode = true)
    val invalidV2 = runCatching { loader.load(root) }.exceptionOrNull()
    require(invalidV2 is SitePackageValidationException) { "Site Package v2 renderMode compatibility leakage must fail closed" }
}

private data class AdoptionPageState(
    val name: String,
    val bodyHtml: String,
    val contentModel: String,
    val rendererKey: String,
    val contentOwner: String,
    val structuredPayload: String?,
    val embedUrl: String?,
    val sortOrder: Int,
) {
    fun matchesLegacy(body: String, renderer: String, url: String?): Boolean =
        bodyHtml == body && rendererKey == renderer && structuredPayload == null && embedUrl == url &&
            if (renderer == "RICH_TEXT") contentModel == "RICH_TEXT" && contentOwner == "OPERATOR" else true
}

private fun assertStructuredState(state: AdoptionPageState, objectMapper: ObjectMapper) {
    require(state.bodyHtml.isEmpty()) { "Structured Page must not keep whole-page bodyHtml: $state" }
    require(state.contentModel == "STRUCTURED" && state.rendererKey == "JILINJOBS_GUIDE_CARDS" && state.contentOwner == "OPERATOR") { "Structured contract mismatch: $state" }
    require(state.embedUrl == null)
    val payload = objectMapper.readValue(requireNotNull(state.structuredPayload), PageStructuredContent::class.java)
    require(payload.schemaVersion == 1 && payload.kind == "CARD_COLLECTION")
    require(payload.items.map { it.title } == listOf("第一项", "第二项", "第三项")) { "Structured item order mismatch: $payload" }
    require(payload.items[1].bodyHtml.contains("/static/pages/guide/jypq/test-1.png"))
}

private fun deleteTestPage(dataSource: DataSource, alias: String) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("DELETE FROM cms_page WHERE group_id IS NULL AND alias=?").use { statement ->
            statement.setString(1, alias)
            statement.executeUpdate()
        }
    }
}

private fun readPage(dataSource: DataSource, alias: String): AdoptionPageState = dataSource.connection.use { connection ->
    connection.prepareStatement(
        "SELECT name,body_html,content_model,renderer_key,content_owner,structured_payload,embed_url,sort_order FROM cms_page WHERE group_id IS NULL AND alias=?",
    ).use { statement ->
        statement.setString(1, alias)
        statement.executeQuery().use { result ->
            require(result.next())
            AdoptionPageState(
                result.getString("name"), result.getString("body_html"), result.getString("content_model"), result.getString("renderer_key"),
                result.getString("content_owner"), result.getString("structured_payload"), result.getString("embed_url"), result.getInt("sort_order"),
            )
        }
    }
}

private fun writeLegacyPackage(root: Path, alias: String, name: String, body: String, sortOrder: Int, adoptionFingerprint: String?) {
    val structure = root.resolve("structure")
    Files.createDirectories(structure)
    val pages = structure.resolve("pages.json")
    val adoption = adoptionFingerprint?.let { ",\"contentAdoptionFromFingerprint\":\"$it\"" } ?: ""
    Files.writeString(
        pages,
        "[{\"groupAlias\":null,\"alias\":\"$alias\",\"name\":\"$name\",\"bodyHtml\":\"$body\",\"renderMode\":\"RICH_TEXT\",\"embedUrl\":null$adoption,\"sortOrder\":$sortOrder,\"enabled\":true,\"preset\":true}]\n",
    )
    writeManifest(root, 1, "legacy-page-adoption", pages)
}

private fun writeStructuredPackage(
    root: Path,
    name: String,
    sortOrder: Int,
    adoptionFingerprint: String?,
    includeLegacyRenderMode: Boolean = false,
) {
    val structure = root.resolve("structure")
    Files.createDirectories(structure)
    val pages = structure.resolve("pages.json")
    val adoption = adoptionFingerprint?.let { ",\"contentAdoptionFromFingerprint\":\"$it\"" } ?: ""
    val legacy = if (includeLegacyRenderMode) ",\"renderMode\":\"RICH_TEXT\"" else ""
    Files.writeString(
        pages,
        """[{"groupAlias":null,"alias":"$STRUCTURED_TEST_ALIAS","name":"$name","bodyHtml":"","contentModel":"STRUCTURED","rendererKey":"JILINJOBS_GUIDE_CARDS","contentOwner":"OPERATOR","structuredPayload":{"schemaVersion":1,"kind":"CARD_COLLECTION","items":[{"title":"第一项","bodyHtml":"<p>正文一</p>"},{"title":"第二项","bodyHtml":"<p><img src=\"/static/pages/guide/jypq/test-1.png\"></p>"},{"title":"第三项","bodyHtml":"<p>正文三</p>"}]},"embedUrl":null$adoption,"sortOrder":$sortOrder,"enabled":true,"preset":true$legacy}]""" + "\n",
    )
    writeManifest(root, 2, "structured-page-adoption", pages)
}

private fun writeManifest(root: Path, schemaVersion: Int, packageId: String, pages: Path) {
    Files.writeString(
        root.resolve("manifest.json"),
        """{"packageId":"$packageId","schemaVersion":$schemaVersion,"version":"1.0.0","structure":[{"type":"pages","path":"structure/pages.json","sha256":"${sha256(pages)}"}]}""" + "\n",
    )
}

private fun legacyFingerprint(objectMapper: ObjectMapper, bodyHtml: String, renderMode: String, embedUrl: String?): String {
    val bytes = objectMapper.writeValueAsBytes(
        linkedMapOf<String, Any?>("bodyHtml" to bodyHtml, "renderMode" to renderMode, "embedUrl" to embedUrl),
    )
    return MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}

private fun sha256(path: Path): String {
    val digest = MessageDigest.getInstance("SHA-256")
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
