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
            "--spring.main.banner-mode=off",
        )
    try {
        val dataSource = context.getBean(DataSource::class.java)
        val provisioner = context.getBean(SitePackageProvisioner::class.java)
        val root = Files.createTempDirectory("eu49-page-ownership-")

        deleteTestPage(dataSource)
        writePagePackage(root, name = "Ownership Page", sortOrder = 10)
        val first = provisioner.apply(root)
        require(first.created == 1 && first.updated == 0 && first.unchanged == 0) { "Fresh Page provision unexpected: $first" }
        require(
            readPage(dataSource) == PageState(
                "Ownership Page", "<p>Package default</p>", "RICH_TEXT", "RICH_TEXT", "OPERATOR", null, null, 10, true,
            ),
        ) { "Fresh Page did not use package defaults: ${readPage(dataSource)}" }

        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "UPDATE cms_page SET body_html=?,content_model=?,renderer_key=?,content_owner=?,structured_payload=?,embed_url=? WHERE group_id IS NULL AND alias='ownership-page'",
            ).use { statement ->
                statement.setString(1, "<p>Operator maintained content</p>")
                statement.setString(2, "NONE")
                statement.setString(3, "INTERNAL_STATIC")
                statement.setString(4, "ENGINEERING")
                statement.setString(5, null)
                statement.setString(6, "/operator-managed")
                require(statement.executeUpdate() == 1)
            }
        }
        val operatorState = readPage(dataSource)

        val second = provisioner.apply(root)
        require(second.created == 0 && second.updated == 0 && second.unchanged == 1) {
            "Ordinary second reconcile must treat operator Page content as unchanged ownership: $second"
        }
        require(second.protectedPageContent == listOf("<root>:ownership-page")) { "Protected content report missing: $second" }
        require(readPage(dataSource) == operatorState) { "Second reconcile overwrote operator-owned Page content" }

        writePagePackage(root, name = "Ownership Page Renamed", sortOrder = 20)
        val structural = provisioner.apply(root)
        require(structural.created == 0 && structural.updated == 1 && structural.unchanged == 0) {
            "Structural Page reconcile unexpected: $structural"
        }
        require(
            readPage(dataSource) == operatorState.copy(name = "Ownership Page Renamed", sortOrder = 20),
        ) { "Structural reconcile did not preserve operator-owned Page content: ${readPage(dataSource)}" }

        println("EU49_PAGE_CONTENT_OWNERSHIP_VERIFY PASS")
    } finally {
        context.close()
    }
}

private data class PageState(
    val name: String,
    val bodyHtml: String,
    val contentModel: String,
    val rendererKey: String,
    val contentOwner: String,
    val structuredPayload: String?,
    val embedUrl: String?,
    val sortOrder: Int,
    val enabled: Boolean,
)

private fun deleteTestPage(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("DELETE FROM cms_page WHERE group_id IS NULL AND alias='ownership-page'").use { it.executeUpdate() }
    }
}

private fun readPage(dataSource: DataSource): PageState = dataSource.connection.use { connection ->
    connection.prepareStatement(
        "SELECT name,body_html,content_model,renderer_key,content_owner,structured_payload,embed_url,sort_order,enabled FROM cms_page WHERE group_id IS NULL AND alias='ownership-page'",
    ).use { statement ->
        statement.executeQuery().use { result ->
            require(result.next()) { "Ownership Page missing" }
            PageState(
                result.getString("name"), result.getString("body_html"), result.getString("content_model"), result.getString("renderer_key"),
                result.getString("content_owner"), result.getString("structured_payload"), result.getString("embed_url"), result.getInt("sort_order"), result.getBoolean("enabled"),
            )
        }
    }
}

private fun writePagePackage(root: Path, name: String, sortOrder: Int) {
    val structure = root.resolve("structure")
    Files.createDirectories(structure)
    val pages = structure.resolve("pages.json")
    Files.writeString(
        pages,
        """
        [
          {
            "groupAlias": null,
            "alias": "ownership-page",
            "name": "$name",
            "bodyHtml": "<p>Package default</p>",
            "renderMode": "RICH_TEXT",
            "embedUrl": null,
            "sortOrder": $sortOrder,
            "enabled": true,
            "preset": true
          }
        ]
        """.trimIndent() + "\n",
    )
    Files.writeString(
        root.resolve("manifest.json"),
        """{"packageId":"eu49-page-ownership","schemaVersion":1,"version":"1.0.0","structure":[{"type":"pages","path":"structure/pages.json","sha256":"${sha256(pages)}"}]}""" + "\n",
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
