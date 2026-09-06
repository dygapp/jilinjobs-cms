package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import javax.sql.DataSource

private val BOOTSTRAP_ID = Regex("[a-z0-9][a-z0-9-]{0,63}")
private val BOOTSTRAP_SHA256 = Regex("[0-9a-f]{64}")

data class SiteBootstrapManifest(
    val bootstrapId: String = "",
    val script: String = "",
    val sha256: String = "",
)

data class SiteBootstrapReport(
    val packageId: String,
    val bootstrapId: String,
    val status: String,
    val contentSha256: String,
    val appliedContentSha256: String,
)

@Service
class SitePackageBootstrapper(
    private val sitePackageLoader: SitePackageLoader,
    private val objectMapper: ObjectMapper,
    private val dataSource: DataSource,
) {
    fun apply(packageRoot: Path): SiteBootstrapReport {
        val root = packageRoot.toAbsolutePath().normalize()
        val packageId = sitePackageLoader.load(root).manifest.packageId
        val bootstrapRoot = root.resolve("bootstrap").normalize()
        if (!bootstrapRoot.startsWith(root) || !Files.isDirectory(bootstrapRoot)) {
            throw SitePackageValidationException("Site Package bootstrap 目录不存在：bootstrap")
        }
        val manifestPath = bootstrapRoot.resolve("manifest.json").normalize()
        if (!manifestPath.startsWith(bootstrapRoot) || !Files.isRegularFile(manifestPath)) {
            throw SitePackageValidationException("Site Package bootstrap manifest 不存在")
        }
        val manifest = objectMapper.readValue(manifestPath.toFile(), SiteBootstrapManifest::class.java)
        validateManifest(manifest)
        val scriptPath = bootstrapRoot.resolve(manifest.script).normalize()
        if (!scriptPath.startsWith(bootstrapRoot) || !Files.isRegularFile(scriptPath)) {
            throw SitePackageValidationException("Site Package bootstrap script 不存在或路径越界：${manifest.script}")
        }
        val actualSha256 = sha256(scriptPath)
        if (actualSha256 != manifest.sha256) {
            throw SitePackageValidationException("Site Package bootstrap digest 不一致：${manifest.script}")
        }

        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val appliedDigest = connection.prepareStatement(
                    "SELECT content_sha256 FROM cms_site_bootstrap_state WHERE package_id=? AND bootstrap_id=?",
                ).use { statement ->
                    statement.setString(1, packageId)
                    statement.setString(2, manifest.bootstrapId)
                    statement.executeQuery().use { result -> if (result.next()) result.getString(1) else null }
                }
                if (appliedDigest != null) {
                    connection.commit()
                    return SiteBootstrapReport(
                        packageId = packageId,
                        bootstrapId = manifest.bootstrapId,
                        status = "ALREADY_APPLIED",
                        contentSha256 = actualSha256,
                        appliedContentSha256 = appliedDigest,
                    )
                }

                ScriptUtils.executeSqlScript(connection, FileSystemResource(scriptPath.toFile()))
                connection.prepareStatement(
                    "INSERT INTO cms_site_bootstrap_state(package_id,bootstrap_id,content_sha256) VALUES(?,?,?)",
                ).use { statement ->
                    statement.setString(1, packageId)
                    statement.setString(2, manifest.bootstrapId)
                    statement.setString(3, actualSha256)
                    statement.executeUpdate()
                }
                connection.commit()
                return SiteBootstrapReport(
                    packageId = packageId,
                    bootstrapId = manifest.bootstrapId,
                    status = "APPLIED",
                    contentSha256 = actualSha256,
                    appliedContentSha256 = actualSha256,
                )
            } catch (error: Exception) {
                connection.rollback()
                throw error
            }
        }
    }

    private fun validateManifest(manifest: SiteBootstrapManifest) {
        if (!manifest.bootstrapId.matches(BOOTSTRAP_ID)) {
            throw SitePackageValidationException("bootstrapId 不合法：${manifest.bootstrapId}")
        }
        if (manifest.script.isBlank()) throw SitePackageValidationException("bootstrap script 不能为空")
        if (!manifest.sha256.matches(BOOTSTRAP_SHA256)) {
            throw SitePackageValidationException("bootstrap sha256 不合法")
        }
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
}

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "cms.site-package", name = ["root"])
class SitePackageBootstrapRuntimeConfiguration {
    @Bean
    @DependsOn("sitePackageRuntimeComposition")
    @ConditionalOnProperty(prefix = "cms.site-package", name = ["bootstrap-on-start"], havingValue = "true")
    fun sitePackageBootstrapRuntime(
        @Value("\${cms.site-package.root}") configuredRoot: String,
        bootstrapper: SitePackageBootstrapper,
    ): SitePackageBootstrapRuntime {
        val root = configuredRoot.trim()
        require(root.isNotEmpty()) { "cms.site-package.root 不能为空" }
        return SitePackageBootstrapRuntime(Path.of(root).toAbsolutePath().normalize(), bootstrapper)
    }
}

class SitePackageBootstrapRuntime(
    val packageRoot: Path,
    bootstrapper: SitePackageBootstrapper,
) {
    val report: SiteBootstrapReport = bootstrapper.apply(packageRoot)

    init {
        LOGGER.info(
            "Site Package bootstrap evaluated: packageId={}, bootstrapId={}, status={}, digest={}, root={}",
            report.packageId,
            report.bootstrapId,
            report.status,
            report.appliedContentSha256,
            packageRoot,
        )
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SitePackageBootstrapRuntime::class.java)
    }
}

object SitePackageBootstrapCli {
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.isNotEmpty()) { "用法：bootstrapSitePackage <site-package-root> [Spring Boot args...]" }
        val packageRoot = Path.of(args.first()).toAbsolutePath().normalize()
        val context = SpringApplicationBuilder(CmsApplication::class.java)
            .web(WebApplicationType.NONE)
            .run(*args.drop(1).toTypedArray())
        try {
            val provision = context.getBean(SitePackageProvisioner::class.java).apply(packageRoot)
            val bootstrap = context.getBean(SitePackageBootstrapper::class.java).apply(packageRoot)
            println("SITE_PACKAGE_PROVISION_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(provision)}")
            println("SITE_PACKAGE_BOOTSTRAP_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(bootstrap)}")
        } finally {
            context.close()
        }
    }
}
