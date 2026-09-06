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
        provisioner: SitePackageProvisioner,
    ): SitePackageRuntimeComposition {
        val root = configuredRoot.trim()
        require(root.isNotEmpty()) { "cms.site-package.root 不能为空" }
        return SitePackageRuntimeComposition(Path.of(root).toAbsolutePath().normalize(), provisioner)
    }
}

class SitePackageRuntimeComposition(
    val packageRoot: Path,
    provisioner: SitePackageProvisioner,
) {
    val report: SiteProvisioningReport = provisioner.apply(packageRoot)

    init {
        LOGGER.info(
            "Site Package runtime composition applied: packageId={}, version={}, created={}, updated={}, unchanged={}, objects={}, root={}",
            report.packageId,
            report.version,
            report.created,
            report.updated,
            report.unchanged,
            report.objects,
            packageRoot,
        )
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SitePackageRuntimeComposition::class.java)
    }
}
