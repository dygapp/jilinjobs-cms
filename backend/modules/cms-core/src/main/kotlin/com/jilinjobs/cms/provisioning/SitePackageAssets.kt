package com.jilinjobs.cms.provisioning

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.security.MessageDigest

private const val SITE_PACKAGE_ASSET_SCHEMA_VERSION = 1
private val ASSET_SHA256 = Regex("[0-9a-f]{64}")

data class SitePackageAssetManifest(
    val packageId: String = "",
    val schemaVersion: Int = 0,
    val assets: List<SitePackageAsset> = emptyList(),
)

data class SitePackageAsset(
    val source: String = "",
    val target: String = "",
    val sha256: String = "",
)

data class SitePackageAssetDefinition(
    val packageId: String,
    val packageRoot: Path,
    val assets: List<SitePackageAsset>,
)

data class SitePackageAssetProjectionReport(
    val packageId: String,
    val created: Int,
    val unchanged: Int,
    val assets: Int,
)

@Service
class SitePackageAssetManifestLoader(
    private val sitePackageLoader: SitePackageLoader,
    private val objectMapper: ObjectMapper,
) {
    fun load(packageRoot: Path): SitePackageAssetDefinition {
        val root = packageRoot.toAbsolutePath().normalize()
        val packageManifest = sitePackageLoader.load(root).manifest
        val manifestPath = safeSource(root, "assets/manifest.json")
        val manifest = objectMapper.readValue(manifestPath.toFile(), SitePackageAssetManifest::class.java)
        if (manifest.schemaVersion != SITE_PACKAGE_ASSET_SCHEMA_VERSION) {
            throw SitePackageValidationException("不支持的 Site Package asset schemaVersion：${manifest.schemaVersion}")
        }
        if (manifest.packageId != packageManifest.packageId) {
            throw SitePackageValidationException("Site Package asset packageId 与主 manifest 不一致：${manifest.packageId}")
        }
        if (manifest.assets.isEmpty()) throw SitePackageValidationException("Site Package assets 不能为空")
        if (manifest.assets.map { it.source }.toSet().size != manifest.assets.size) {
            throw SitePackageValidationException("Site Package asset source 不能重复")
        }
        if (manifest.assets.map { it.target }.toSet().size != manifest.assets.size) {
            throw SitePackageValidationException("Site Package asset target 不能重复")
        }
        manifest.assets.forEach { asset ->
            if (!asset.source.startsWith("assets/")) {
                throw SitePackageValidationException("Site Package asset source 必须位于 assets/：${asset.source}")
            }
            if (asset.source == "assets/manifest.json") {
                throw SitePackageValidationException("Site Package asset manifest 不能投影自身")
            }
            val source = safeSource(root, asset.source)
            staticRelativePath(asset.target)
            if (!asset.sha256.matches(ASSET_SHA256)) {
                throw SitePackageValidationException("Site Package asset sha256 不合法：${asset.source}")
            }
            if (sha256(source) != asset.sha256) {
                throw SitePackageValidationException("Site Package asset digest 不一致：${asset.source}")
            }
        }
        return SitePackageAssetDefinition(packageManifest.packageId, root, manifest.assets)
    }

    private fun safeSource(root: Path, relativePath: String): Path {
        val normalizedText = relativePath.trim().replace('\\', '/')
        if (normalizedText.isBlank() || normalizedText.startsWith('/') || normalizedText.split('/').any { it.isBlank() || it == "." || it == ".." }) {
            throw SitePackageValidationException("Site Package asset source 路径不合法：$relativePath")
        }
        val source = root.resolve(normalizedText).normalize()
        if (!source.startsWith(root) || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(source)) {
            throw SitePackageValidationException("Site Package asset source 不存在、越界或为符号链接：$relativePath")
        }
        val realRoot = root.toRealPath()
        val realSource = source.toRealPath()
        if (!realSource.startsWith(realRoot)) {
            throw SitePackageValidationException("Site Package asset source 实际路径越界：$relativePath")
        }
        return source
    }
}

@Service
class SitePackageAssetProjector(private val loader: SitePackageAssetManifestLoader) {
    fun project(packageRoot: Path, staticRoot: Path): SitePackageAssetProjectionReport =
        project(loader.load(packageRoot), staticRoot)

    fun project(definition: SitePackageAssetDefinition, configuredStaticRoot: Path): SitePackageAssetProjectionReport {
        val staticRoot = configuredStaticRoot.toAbsolutePath().normalize()
        Files.createDirectories(staticRoot)
        val realStaticRoot = staticRoot.toRealPath()
        var created = 0
        var unchanged = 0
        definition.assets.forEach { asset ->
            val relative = staticRelativePath(asset.target)
            val target = staticRoot.resolve(relative).normalize()
            if (!target.startsWith(staticRoot)) {
                throw SitePackageValidationException("Site Package asset target 越界：${asset.target}")
            }
            Files.createDirectories(target.parent)
            if (!target.parent.toRealPath().startsWith(realStaticRoot)) {
                throw SitePackageValidationException("Site Package asset target 实际路径越界：${asset.target}")
            }
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(target)) {
                    throw SitePackageValidationException("Site Package asset target 已被非普通文件占用：${asset.target}")
                }
                unchanged++
            } else {
                val source = definition.packageRoot.resolve(asset.source).normalize()
                Files.copy(source, target)
                created++
            }
        }
        return SitePackageAssetProjectionReport(definition.packageId, created, unchanged, definition.assets.size)
    }
}

@Service
class SitePackageAssetCatalog(
    @Value("\${cms.site-package.root:}") configuredRoot: String,
    loader: SitePackageAssetManifestLoader,
) {
    val protectedPaths: Set<String> = configuredRoot.trim().takeIf { it.isNotEmpty() }
        ?.let { root -> loader.load(Path.of(root).toAbsolutePath().normalize()).assets }
        ?.mapTo(linkedSetOf()) { staticRelativePath(it.target) }
        ?: emptySet()
}

internal fun staticRelativePath(target: String): String {
    val normalized = target.trim().replace('\\', '/')
    if (!normalized.startsWith("/static/")) {
        throw SitePackageValidationException("Site Package asset target 必须位于 /static/**：$target")
    }
    val relative = normalized.removePrefix("/static/")
    if (relative.isBlank() || relative.split('/').any { it.isBlank() || it == "." || it == ".." }) {
        throw SitePackageValidationException("Site Package asset target 路径不合法：$target")
    }
    if (relative == "uploads" || relative.startsWith("uploads/")) {
        throw SitePackageValidationException("Site Package 不得接管 /static/uploads/**：$target")
    }
    return relative
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
