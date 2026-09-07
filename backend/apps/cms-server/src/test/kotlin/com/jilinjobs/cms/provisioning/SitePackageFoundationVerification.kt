package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import javax.sql.DataSource

fun main() {
    val dbUrl = requireFoundationEnv("SITE_PACKAGE_VERIFY_DB_URL")
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

        insertFoundationOperatorColumn(dataSource)
        val validRoot = Files.createTempDirectory("eu37-valid-package-")
        writeFoundationPackage(
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
        verifyFoundationRuntimeState(dataSource)

        writeFoundationPackage(
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

        expectFoundationValidation("operator ownership conflict") {
            val root = Files.createTempDirectory("eu37-conflict-package-")
            writeFoundationPackage(root, """[{"alias":"operator-owned","name":"Attempted Takeover","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true}]""" + "\n")
            provisioner.apply(root)
        }
        expectFoundationValidation("cycle") {
            val root = Files.createTempDirectory("eu37-cycle-package-")
            writeFoundationPackage(root, """[{"alias":"cycle-a","name":"A","parentAlias":"cycle-b","coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true},{"alias":"cycle-b","name":"B","parentAlias":"cycle-a","coverPolicy":"OPTIONAL","sortOrder":2,"enabled":true,"preset":true}]""" + "\n")
            provisioner.apply(root)
        }
        expectFoundationValidation("duplicate alias") {
            val root = Files.createTempDirectory("eu37-duplicate-package-")
            writeFoundationPackage(root, """[{"alias":"duplicate","name":"A","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true},{"alias":"duplicate","name":"B","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":2,"enabled":true,"preset":true}]""" + "\n")
            provisioner.apply(root)
        }
        expectFoundationValidation("missing parent") {
            val root = Files.createTempDirectory("eu37-parent-package-")
            writeFoundationPackage(root, """[{"alias":"orphan","name":"Orphan","parentAlias":"missing-parent","coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true}]""" + "\n")
            provisioner.apply(root)
        }
        expectFoundationValidation("digest") {
            val root = Files.createTempDirectory("eu37-digest-package-")
            writeFoundationPackage(root, """[{"alias":"digest","name":"Digest","parentAlias":null,"coverPolicy":"OPTIONAL","sortOrder":1,"enabled":true,"preset":true}]""" + "\n", digestOverride = "0".repeat(64))
            provisioner.apply(root)
        }
        expectFoundationValidation("path traversal") {
            val root = Files.createTempDirectory("eu37-path-package-")
            val outside = root.parent.resolve("eu37-outside-columns.json")
            Files.writeString(outside, "[]\n")
            Files.writeString(
                root.resolve("manifest.json"),
                """{"packageId":"foundation-test","schemaVersion":1,"version":"1.0.0","structure":[{"type":"columns","path":"../eu37-outside-columns.json","sha256":"${sha256Foundation(outside)}"}]}""" + "\n",
            )
            provisioner.apply(root)
        }

        verifyFoundationOperatorColumnUnchanged(dataSource)
        println("EU37_SITE_PACKAGE_FOUNDATION_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun writeFoundationPackage(root: Path, columnsJson: String, digestOverride: String? = null) {
    val structure = root.resolve("structure")
    Files.createDirectories(structure)
    val columns = structure.resolve("columns.json")
    Files.writeString(columns, columnsJson)
    val digest = digestOverride ?: sha256Foundation(columns)
    Files.writeString(
        root.resolve("manifest.json"),
        """{"packageId":"foundation-test","schemaVersion":1,"version":"1.0.0","structure":[{"type":"columns","path":"structure/columns.json","sha256":"$digest"}]}""" + "\n",
    )
}

private fun insertFoundationOperatorColumn(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("INSERT INTO cms_column(parent_id,alias,name,cover_policy,sort_order,enabled,preset) VALUES(NULL,'operator-owned','运营栏目','OPTIONAL',5,1,0)").use { it.executeUpdate() }
    }
}

private fun verifyFoundationRuntimeState(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT child.preset child_preset,parent.preset parent_preset,parent.alias parent_alias FROM cms_column child JOIN cms_column parent ON parent.id=child.parent_id WHERE child.alias='foundation-child'").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next())
                require(result.getBoolean("child_preset"))
                require(result.getBoolean("parent_preset"))
                require(result.getString("parent_alias") == "foundation-root")
            }
        }
    }
}

private fun verifyFoundationOperatorColumnUnchanged(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT name,preset FROM cms_column WHERE alias='operator-owned'").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next() && result.getString("name") == "运营栏目" && !result.getBoolean("preset"))
            }
        }
    }
}

private fun expectFoundationValidation(label: String, action: () -> Unit) {
    val error = runCatching(action).exceptionOrNull()
    require(error is SitePackageValidationException) { "$label 应被 SitePackageValidationException 拒绝，实际：${error?.javaClass?.name}" }
}

private fun requireFoundationEnv(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")

private fun sha256Foundation(path: Path): String {
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
