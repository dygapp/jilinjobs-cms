package com.jilinjobs.cms.provisioning

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import java.nio.file.Path

@Configuration(proxyBeanMethods = false)
class SitePackageRuntimeCompositionConfiguration {
    @Bean
    @DependsOn("flywayInitializer")
    @ConditionalOnProperty(prefix = "cms.site-package", name = ["root"])
    fun sitePackageRuntimeComposition(
        @Value("\${cms.site-package.root}") configuredRoot: String,
        @Value("\${cms.static.root:./data/static}") configuredStaticRoot: String,
        provisioner: SitePackageProvisioner,
        assetProjector: SitePackageAssetProjector,
    ): SitePackageRuntimeComposition {
        val root = configuredRoot.trim()
        require(root.isNotEmpty()) { "cms.site-package.root 不能为空" }
        val staticRoot = configuredStaticRoot.trim()
        require(staticRoot.isNotEmpty()) { "cms.static.root 不能为空" }
        return SitePackageRuntimeComposition(
            Path.of(root).toAbsolutePath().normalize(),
            Path.of(staticRoot).toAbsolutePath().normalize(),
            provisioner,
            assetProjector,
        )
    }
}

class SitePackageRuntimeComposition(
    val packageRoot: Path,
    val staticRoot: Path,
    provisioner: SitePackageProvisioner,
    assetProjector: SitePackageAssetProjector,
) {
    val report: SiteProvisioningReport = provisioner.apply(packageRoot)
    val assetReport: SitePackageAssetProjectionReport = assetProjector.project(packageRoot, staticRoot)

    init {
        LOGGER.info(
            "Site Package runtime composition applied: packageId={}, version={}, created={}, updated={}, unchanged={}, objects={}, assetsCreated={}, assetsUnchanged={}, assets={}, root={}, staticRoot={}",
            report.packageId,
            report.version,
            report.created,
            report.updated,
            report.unchanged,
            report.objects,
            assetReport.created,
            assetReport.unchanged,
            assetReport.assets,
            packageRoot,
            staticRoot,
        )
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SitePackageRuntimeComposition::class.java)
    }
}
