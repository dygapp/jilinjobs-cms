package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import tools.jackson.databind.ObjectMapper
import java.nio.file.Path

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
