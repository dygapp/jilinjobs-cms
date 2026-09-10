package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.staticresource.StaticResourceService
import com.jilinjobs.cms.staticresource.StaticResourceValidationException
import org.flywaydb.core.Flyway
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.ObjectMapper
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

fun main() {
    val dbUrl = requireAssetEnv("SITE_PACKAGE_VERIFY_DB_URL")
    val dbUsername = System.getenv("SITE_PACKAGE_VERIFY_DB_USERNAME") ?: "root"
    val dbPassword = System.getenv("SITE_PACKAGE_VERIFY_DB_PASSWORD") ?: "root"
    val packageRoot = Path.of("../sites/jilinjobs").toAbsolutePath().normalize()
    val staticRoot = Files.createTempDirectory("eu42-runtime-static-")

    Flyway.configure()
        .dataSource(dbUrl, dbUsername, dbPassword)
        .locations("classpath:db/migration")
        .cleanDisabled(false)
        .load()
        .also { it.clean(); it.migrate() }

    val first = startAssetContext(dbUrl, dbUsername, dbPassword, packageRoot, staticRoot)
    lateinit var replacement: ByteArray
    lateinit var uploadBytes: ByteArray
    try {
        val composition = first.getBean(SitePackageRuntimeComposition::class.java)
        require(composition.assetReport.assets == 187)
        require(composition.assetReport.created == 187 && composition.assetReport.unchanged == 0) {
            "fresh Runtime projection 应创建完整稳定资源：${composition.assetReport}"
        }
        val catalog = first.getBean(SitePackageAssetCatalog::class.java)
        require(catalog.protectedPaths.size == 187)
        require("home/header-banner.png" in catalog.protectedPaths)
        require("party/party-header-banner.jpg" in catalog.protectedPaths)
        require(catalog.protectedPaths.none { it == "uploads" || it.startsWith("uploads/") })

        val service = first.getBean(StaticResourceService::class.java)
        require(Files.isRegularFile(service.resolvePublic("home/header-banner.png")))
        require(Files.isRegularFile(service.resolvePublic("party/party-header-banner.jpg")))
        require(Files.isRegularFile(service.resolvePublic("brand/site-favicon.png")))
        val deleteError = runCatching { service.delete("home/header-banner.png") }.exceptionOrNull()
        require(deleteError is StaticResourceValidationException) { "Site Package stable target 必须阻止普通删除" }

        val original = Files.readAllBytes(staticRoot.resolve("health/baseline.png"))
        replacement = original + byteArrayOf(0x01)
        val replaced = service.upload("health/baseline.png", AssetBytesMultipartFile("baseline.png", replacement), true)
        require(replaced.protectedResource)
        require(Files.readAllBytes(staticRoot.resolve("health/baseline.png")).contentEquals(replacement))

        uploadBytes = original + byteArrayOf(0x02)
        val uploaded = service.upload("uploads/operator.png", AssetBytesMultipartFile("operator.png", uploadBytes), false)
        require(!uploaded.protectedResource)

        val projector = first.getBean(SitePackageAssetProjector::class.java)
        val secondProjection = projector.project(packageRoot, staticRoot)
        require(secondProjection.created == 0 && secondProjection.unchanged == 187) {
            "第二次 projection 必须幂等：$secondProjection"
        }
        require(Files.readAllBytes(staticRoot.resolve("health/baseline.png")).contentEquals(replacement)) {
            "operator 明确替换必须被 projection 保留"
        }
        require(Files.readAllBytes(staticRoot.resolve("uploads/operator.png")).contentEquals(uploadBytes)) {
            "/static/uploads/** 不得被 Site Package 接管"
        }

        Files.delete(staticRoot.resolve("footer/wechat-qr.png"))
        val repair = projector.project(packageRoot, staticRoot)
        require(repair.created == 1 && repair.unchanged == 186) { "缺失 package target 应按 create-if-missing 恢复：$repair" }
        require(Files.readAllBytes(staticRoot.resolve("health/baseline.png")).contentEquals(replacement))
        require(Files.readAllBytes(staticRoot.resolve("uploads/operator.png")).contentEquals(uploadBytes))

        verifyAssetManifestFailures(first.getBean(SitePackageAssetManifestLoader::class.java), first.getBean(ObjectMapper::class.java), packageRoot)
    } finally {
        first.close()
    }

    val second = startAssetContext(dbUrl, dbUsername, dbPassword, packageRoot, staticRoot)
    try {
        val composition = second.getBean(SitePackageRuntimeComposition::class.java)
        require(composition.assetReport.created == 0 && composition.assetReport.unchanged == 187) {
            "restart projection 必须保持现有 target：${composition.assetReport}"
        }
        require(Files.readAllBytes(staticRoot.resolve("health/baseline.png")).contentEquals(replacement))
        require(Files.readAllBytes(staticRoot.resolve("uploads/operator.png")).contentEquals(uploadBytes))
        val service = second.getBean(StaticResourceService::class.java)
        require(!service.list("uploads").single { it.name == "operator.png" }.protectedResource)
    } finally {
        second.close()
    }

    println("EU42_SITE_PACKAGE_ASSET_PROJECTION_VERIFY PASS")
}

private fun startAssetContext(dbUrl: String, username: String, password: String, packageRoot: Path, staticRoot: Path) =
    SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=$username",
            "--spring.datasource.password=$password",
            "--cms.site-package.root=$packageRoot",
            "--cms.static.root=$staticRoot",
            "--spring.main.banner-mode=off",
        )

private fun verifyAssetManifestFailures(loader: SitePackageAssetManifestLoader, objectMapper: ObjectMapper, sourceRoot: Path) {
    fun mutated(transform: (SitePackageAssetManifest) -> SitePackageAssetManifest): Path {
        val root = Files.createTempDirectory("eu42-invalid-package-")
        copyTree(sourceRoot, root)
        val path = root.resolve("assets/manifest.json")
        val manifest = objectMapper.readValue(path.toFile(), SitePackageAssetManifest::class.java)
        objectMapper.writeValue(path.toFile(), transform(manifest))
        return root
    }
    expectAssetValidation("digest mismatch") {
        val root = mutated { manifest -> manifest.copy(assets = manifest.assets.mapIndexed { index, asset -> if (index == 0) asset.copy(sha256 = "0".repeat(64)) else asset }) }
        loader.load(root)
    }
    expectAssetValidation("duplicate target") {
        val root = mutated { manifest ->
            manifest.copy(
                assets = manifest.assets.mapIndexed { index, asset ->
                    if (index == 1) asset.copy(target = manifest.assets.first().target) else asset
                },
            )
        }
        loader.load(root)
    }
    expectAssetValidation("path traversal") {
        val root = mutated { manifest -> manifest.copy(assets = manifest.assets.mapIndexed { index, asset -> if (index == 0) asset.copy(source = "assets/../outside.png") else asset }) }
        loader.load(root)
    }
    expectAssetValidation("package identity") {
        val root = mutated { manifest -> manifest.copy(packageId = "other-site") }
        loader.load(root)
    }
}

private fun copyTree(sourceRoot: Path, targetRoot: Path) {
    Files.walk(sourceRoot).use { stream ->
        stream.forEach { source ->
            val target = targetRoot.resolve(sourceRoot.relativize(source).toString())
            if (Files.isDirectory(source)) Files.createDirectories(target)
            else Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

private fun expectAssetValidation(label: String, action: () -> Unit) {
    val error = runCatching(action).exceptionOrNull()
    require(error is SitePackageValidationException) { "$label 应被 SitePackageValidationException 拒绝，实际：${error?.javaClass?.name}" }
}

private fun requireAssetEnv(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")

private class AssetBytesMultipartFile(private val filename: String, private val data: ByteArray) : MultipartFile {
    override fun getName() = "file"
    override fun getOriginalFilename() = filename
    override fun getContentType(): String? = null
    override fun isEmpty() = data.isEmpty()
    override fun getSize() = data.size.toLong()
    override fun getBytes() = data
    override fun getInputStream(): InputStream = ByteArrayInputStream(data)
    override fun transferTo(dest: File) { dest.parentFile?.mkdirs(); dest.writeBytes(data) }
}
