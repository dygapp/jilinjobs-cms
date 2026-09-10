package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import javax.sql.DataSource
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder

fun main() {
    val dbUrl = requireNotNull(System.getenv("SITE_PACKAGE_VERIFY_DB_URL")) { "SITE_PACKAGE_VERIFY_DB_URL is required" }
    val context = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=${System.getenv("SITE_PACKAGE_VERIFY_DB_USERNAME") ?: "root"}",
            "--spring.datasource.password=${System.getenv("SITE_PACKAGE_VERIFY_DB_PASSWORD") ?: "root"}",
            "--spring.flyway.target=1",
            "--spring.main.banner-mode=off",
        )
    try {
        val dataSource = context.getBean(DataSource::class.java)
        val provisioner = context.getBean(SitePackageProvisioner::class.java)
        val loader = context.getBean(SitePackageLoader::class.java)
        val root = Files.createTempDirectory("eu52-page-adoption-")
        val oldBody = "<p>关于我们内容可通过固定页面管理维护。</p>"
        val acceptedOldFingerprint = "7b0edda0a0731b89e578d442cdf9e243dccf7e97eaa8d867aeb068742733904e"

        writePackage(root, "About", oldBody, 10, null)
        val first = provisioner.apply(root)
        require(first.created == 1 && first.updated == 0 && first.unchanged == 0)
        require(first.adoptedPageContent.isEmpty() && first.protectedPageContent.isEmpty())
        require(readPage(dataSource).bodyHtml == oldBody)

        writePackage(root, "About", "<p>Formal package content</p>", 10, acceptedOldFingerprint)
        val adopted = provisioner.apply(root)
        require(adopted.created == 0 && adopted.updated == 1 && adopted.unchanged == 0) { "Adoption counters unexpected: $adopted" }
        require(adopted.adoptedPageContent == listOf("<root>:about")) { "Adoption identity report unexpected: $adopted" }
        require(adopted.protectedPageContent.isEmpty())
        require(readPage(dataSource).bodyHtml == "<p>Formal package content</p>")

        val idempotent = provisioner.apply(root)
        require(idempotent.created == 0 && idempotent.updated == 0 && idempotent.unchanged == 1) { "Adoption rerun not idempotent: $idempotent" }
        require(idempotent.adoptedPageContent.isEmpty() && idempotent.protectedPageContent.isEmpty())

        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE cms_page SET body_html=?,render_mode=?,embed_url=? WHERE group_id IS NULL AND alias='about'").use {
                it.setString(1, "<p>Operator edit after adoption</p>")
                it.setString(2, "INTERNAL_STATIC")
                it.setString(3, "/operator-owned")
                require(it.executeUpdate() == 1)
            }
        }
        val operator = readPage(dataSource)
        val protected = provisioner.apply(root)
        require(protected.created == 0 && protected.updated == 0 && protected.unchanged == 1)
        require(protected.adoptedPageContent.isEmpty())
        require(protected.protectedPageContent == listOf("<root>:about")) { "Protected identity report missing: $protected" }
        require(readPage(dataSource) == operator) { "Ordinary reconcile overwrote operator content" }

        writePackage(root, "About renamed", "<p>Formal package content</p>", 20, acceptedOldFingerprint)
        val structural = provisioner.apply(root)
        require(structural.created == 0 && structural.updated == 1 && structural.unchanged == 0)
        require(structural.protectedPageContent == listOf("<root>:about"))
        require(readPage(dataSource) == operator.copy(name = "About renamed", sortOrder = 20)) { "Structural reconcile did not preserve protected content" }

        writePackage(root, "Invalid", "<p>Invalid</p>", 10, "not-a-sha")
        val invalid = runCatching { loader.load(root) }.exceptionOrNull()
        require(invalid is SitePackageValidationException) { "Invalid adoption fingerprint must fail closed" }

        println("EU52_PAGE_CONTENT_ADOPTION_VERIFY PASS")
    } finally {
        context.close()
    }
}

private data class AdoptionPageState(
    val name: String,
    val bodyHtml: String,
    val renderMode: String,
    val embedUrl: String?,
    val sortOrder: Int,
)

private fun readPage(dataSource: DataSource): AdoptionPageState = dataSource.connection.use { connection ->
    connection.prepareStatement("SELECT name,body_html,render_mode,embed_url,sort_order FROM cms_page WHERE group_id IS NULL AND alias='about'").use { statement ->
        statement.executeQuery().use { result ->
            require(result.next())
            AdoptionPageState(result.getString("name"), result.getString("body_html"), result.getString("render_mode"), result.getString("embed_url"), result.getInt("sort_order"))
        }
    }
}

private fun writePackage(root: Path, name: String, body: String, sortOrder: Int, adoptionFingerprint: String?) {
    val structure = root.resolve("structure")
    Files.createDirectories(structure)
    val pages = structure.resolve("pages.json")
    val adoption = adoptionFingerprint?.let { ",\"contentAdoptionFromFingerprint\":\"$it\"" } ?: ""
    Files.writeString(
        pages,
        "[{\"groupAlias\":null,\"alias\":\"about\",\"name\":\"$name\",\"bodyHtml\":\"$body\",\"renderMode\":\"RICH_TEXT\",\"embedUrl\":null$adoption,\"sortOrder\":$sortOrder,\"enabled\":true,\"preset\":true}]\n",
    )
    Files.writeString(
        root.resolve("manifest.json"),
        "{\"packageId\":\"eu52-page-adoption\",\"schemaVersion\":1,\"version\":\"1.0.0\",\"structure\":[{\"type\":\"pages\",\"path\":\"structure/pages.json\",\"sha256\":\"${sha256(pages)}\"}]}\n",
    )
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
