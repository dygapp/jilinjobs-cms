package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import javax.sql.DataSource

fun main() {
    val dbUrl = requireEnv("SITE_PACKAGE_VERIFY_DB_URL")
    val dbUsername = System.getenv("SITE_PACKAGE_VERIFY_DB_USERNAME") ?: "root"
    val dbPassword = System.getenv("SITE_PACKAGE_VERIFY_DB_PASSWORD") ?: "root"
    val context = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=$dbUsername",
            "--spring.datasource.password=$dbPassword",
            "--spring.flyway.target=1",
            "--spring.main.banner-mode=off",
        )
    try {
        val dataSource = context.getBean(DataSource::class.java)
        val loader = context.getBean(SitePackageLoader::class.java)
        val provisioner = context.getBean(SitePackageProvisioner::class.java)

        val repositoryPackage = loader.load(Path.of("../sites/jilinjobs"))
        require(repositoryPackage.manifest.packageId == "jilinjobs") { "仓库 Site Package identity 异常" }
        require(repositoryPackage.manifest.schemaVersion == 1) { "仓库 Site Package schemaVersion 异常" }
        require(repositoryPackage.columns.isEmpty()) { "EU-37 不应提前迁移正式 JilinJobs Column baseline" }

        insertOperatorColumn(dataSource, "operator-owned", "运营栏目")

        val validRoot = Files.createTempDirectory("eu37-valid-package-")
        writePackage(
            validRoot,
            """
            [
              {"alias":"foundation-root","name":"Foundation Root","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":10,"enabled":true,"preset":true},
              {"alias":"foundation-child","name":"Foundation Child","parentAlias":"foundation-root","coverPolicy":"REQUIRED","sortOrder":20,"enabled":true,"preset":true}
            ]
            """.trimIndent() + "\n",
        )
        val first = provisioner.apply(validRoot)
        require(first.created == 2 && first.updated == 0 && first.unchanged == 0) { "first apply 结果异常：$first" }
        val second = provisioner.apply(validRoot)
        require(second.created == 0 && second.updated == 0 && second.unchanged == 2) { "second apply 必须幂等：$second" }
        verifyRuntimeState(dataSource)

        writePackage(
            validRoot,
            """
            [
              {"alias":"foundation-root","name":"Foundation Root","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":10,"enabled":true,"preset":true},
              {"alias":"foundation-child","name":"Foundation Child Updated","parentAlias":"foundation-root","coverPolicy":"REQUIRED","sortOrder":30,"enabled":true,"preset":true}
            ]
            """.trimIndent() + "\n",
        )
        val update = provisioner.apply(validRoot)
        require(update.created == 0 && update.updated == 1 && update.unchanged == 1) { "preset reconcile 结果异常：$update" }

        expectValidation("operator ownership conflict") {
            val root = Files.createTempDirectory("eu37-conflict-package-")
            writePackage(
                root,
                """[{"alias":"operator-owned","name":"Attempted Takeover","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true}]""" + "\n",
            )
            provisioner.apply(root)
        }
        expectValidation("cycle") {
            val root = Files.createTempDirectory("eu37-cycle-package-")
            writePackage(
                root,
                """[{"alias":"cycle-a","name":"A","parentAlias":"cycle-b","coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true},{"alias":"cycle-b","name":"B","parentAlias":"cycle-a","coverPolicy":"OPTIONAL","sortOrder":2,"enabled":true,"preset":true}]""" + "\n",
            )
            provisioner.apply(root)
        }
        expectValidation("duplicate alias") {
            val root = Files.createTempDirectory("eu37-duplicate-package-")
            writePackage(
                root,
                """[{"alias":"duplicate","name":"A","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true},{"alias":"duplicate","name":"B","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":2,"enabled":true,"preset":true}]""" + "\n",
            )
            provisioner.apply(root)
        }
        expectValidation("missing parent") {
            val root = Files.createTempDirectory("eu37-parent-package-")
            writePackage(
                root,
                """[{"alias":"orphan","name":"Orphan","parentAlias":"missing-parent","coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true}]""" + "\n",
            )
            provisioner.apply(root)
        }
        expectValidation("digest") {
            val root = Files.createTempDirectory("eu37-digest-package-")
            writePackage(
                root,
                """[{"alias":"digest","name":"Digest","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true}]""" + "\n",
                digestOverride = "0".repeat(64),
            )
            provisioner.apply(root)
        }
        expectValidation("path traversal") {
            val root = Files.createTempDirectory("eu37-path-package-")
            val outside = root.parent.resolve("eu37-outside-columns.json")
            Files.writeString(outside, "[]\n")
            val digest = sha256(outside)
            Files.writeString(
                root.resolve("manifest.json"),
                """{"packageId":"foundation-test","schemaVersion":1,"version":"1.0.0","structure":[{"type":"columns","path":"../eu37-outside-columns.json","sha256":"$digest"}]}""" + "\n",
            )
            provisioner.apply(root)
        }

        verifyOperatorColumnUnchanged(dataSource)
        println("EU37_SITE_PACKAGE_FOUNDATION_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun requireEnv(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")

private fun writePackage(root: Path, columnsJson: String, digestOverride: String? = null) {
    val structure = root.resolve("structure")
    Files.createDirectories(structure)
    val columns = structure.resolve("columns.json")
    Files.writeString(columns, columnsJson)
    val digest = digestOverride ?: sha256(columns)
    Files.writeString(
        root.resolve("manifest.json"),
        """{"packageId":"foundation-test","schemaVersion":1,"version":"1.0.0","structure":[{"type":"columns","path":"structure/columns.json","sha256":"$digest"}]}""" + "\n",
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

private fun insertOperatorColumn(dataSource: DataSource, alias: String, name: String) {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset) VALUES(NULL, ?, ?, 'OPTIONAL', 5, 1, 0)",
        ).use { statement ->
            statement.setString(1, alias)
            statement.setString(2, name)
            statement.executeUpdate()
        }
    }
}

private fun verifyRuntimeState(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
            SELECT child.preset AS child_preset, parent.preset AS parent_preset, parent.alias AS parent_alias
            FROM cms_column child
            JOIN cms_column parent ON parent.id = child.parent_id
            WHERE child.alias='foundation-child'
            """.trimIndent(),
        ).use { statement ->
            statement.executeQuery().use { result ->
                require(result.next()) { "未找到 foundation-child" }
                require(result.getBoolean("child_preset")) { "child 必须 preset=true" }
                require(result.getBoolean("parent_preset")) { "parent 必须 preset=true" }
                require(result.getString("parent_alias") == "foundation-root") { "parent logical identity 未正确解析" }
            }
        }
        connection.prepareStatement("SELECT COUNT(*) FROM cms_column WHERE alias IN ('foundation-root','foundation-child')").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next() && result.getInt(1) == 2) { "second apply 产生了重复 Column" }
            }
        }
    }
}

private fun verifyOperatorColumnUnchanged(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT name, preset FROM cms_column WHERE alias='operator-owned'").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next()) { "operator-created Column 被删除" }
                require(result.getString("name") == "运营栏目") { "operator-created Column 被改写" }
                require(!result.getBoolean("preset")) { "operator-created Column 被错误接管为 preset" }
            }
        }
    }
}

private fun expectValidation(label: String, action: () -> Unit) {
    val error = runCatching(action).exceptionOrNull()
    require(error is SitePackageValidationException) { "$label 应被 SitePackageValidationException 拒绝，实际：${error?.javaClass?.name}" }
}
