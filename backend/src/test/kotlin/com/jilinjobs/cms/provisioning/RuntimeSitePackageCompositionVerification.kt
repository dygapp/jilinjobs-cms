package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.flywaydb.core.Flyway
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Path
import java.sql.DriverManager

fun main() {
    val dbUrl = requireRuntimeCompositionEnv("SITE_PACKAGE_VERIFY_DB_URL")
    val dbUsername = System.getenv("SITE_PACKAGE_VERIFY_DB_USERNAME") ?: "root"
    val dbPassword = System.getenv("SITE_PACKAGE_VERIFY_DB_PASSWORD") ?: "root"
    val packageRoot = Path.of("../sites/jilinjobs").toAbsolutePath().normalize()

    val flyway = Flyway.configure()
        .dataSource(dbUrl, dbUsername, dbPassword)
        .locations("classpath:db/migration")
        .cleanDisabled(false)
        .load()
    flyway.clean()
    flyway.migrate()

    require(operationalCounts(dbUrl, dbUsername, dbPassword) == (0 to 0)) {
        "Generic CMS Flyway 不得创建 Site operational defaults"
    }
    require(countCodedPresetNavigation(dbUrl, dbUsername, dbPassword) == 0) {
        "Generic CMS Flyway 不得创建 JilinJobs navigation rows"
    }

    val first = startRuntimeCompositionContext(dbUrl, dbUsername, dbPassword, packageRoot)
    try {
        val composition = first.getBean(SitePackageRuntimeComposition::class.java)
        require(composition.packageRoot == packageRoot)
        require(composition.report.created == 98 && composition.report.updated == 0 && composition.report.unchanged == 0) {
            "首次 Runtime composition 应从 Generic Schema 创建完整 stable structure：${composition.report}"
        }
        require(composition.report.objects == 98)
        require(countCodedPresetNavigation(dbUrl, dbUsername, dbPassword) == 40)
        require(operationalCounts(dbUrl, dbUsername, dbPassword) == (0 to 0)) {
            "普通 Runtime composition 不得隐式执行 Site bootstrap"
        }
        require(first.getBeansOfType(SitePackageBootstrapRuntime::class.java).isEmpty()) {
            "未配置 bootstrap-on-start 时不得执行 Site bootstrap"
        }
    } finally {
        first.close()
    }

    val second = startRuntimeCompositionContext(dbUrl, dbUsername, dbPassword, packageRoot)
    try {
        val composition = second.getBean(SitePackageRuntimeComposition::class.java)
        require(composition.report.created == 0 && composition.report.updated == 0 && composition.report.unchanged == 98) {
            "第二次 Runtime composition 必须幂等：${composition.report}"
        }
        require(countCodedPresetNavigation(dbUrl, dbUsername, dbPassword) == 40)
        require(operationalCounts(dbUrl, dbUsername, dbPassword) == (0 to 0))
    } finally {
        second.close()
    }

    val disabled = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=$dbUsername",
            "--spring.datasource.password=$dbPassword",
            "--spring.main.banner-mode=off",
        )
    try {
        require(disabled.getBeansOfType(SitePackageRuntimeComposition::class.java).isEmpty()) {
            "未配置 cms.site-package.root 时不得隐式启用 Site Package runtime composition"
        }
        require(disabled.getBeansOfType(SitePackageBootstrapRuntime::class.java).isEmpty()) {
            "未配置 Site Package 时不得隐式启用 bootstrap"
        }
    } finally {
        disabled.close()
    }

    println("EU41_RUNTIME_SITE_PACKAGE_COMPOSITION_VERIFY PASS")
}

private fun startRuntimeCompositionContext(
    dbUrl: String,
    dbUsername: String,
    dbPassword: String,
    packageRoot: Path,
) = SpringApplicationBuilder(CmsApplication::class.java)
    .web(WebApplicationType.NONE)
    .run(
        "--spring.datasource.url=$dbUrl",
        "--spring.datasource.username=$dbUsername",
        "--spring.datasource.password=$dbPassword",
        "--cms.site-package.root=$packageRoot",
        "--spring.main.banner-mode=off",
    )

private fun countCodedPresetNavigation(dbUrl: String, username: String, password: String): Int =
    DriverManager.getConnection(dbUrl, username, password).use { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM cms_navigation WHERE preset=1 AND code IS NOT NULL").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next())
                result.getInt(1)
            }
        }
    }

private fun operationalCounts(dbUrl: String, username: String, password: String): Pair<Int, Int> =
    DriverManager.getConnection(dbUrl, username, password).use { connection ->
        fun count(table: String): Int = connection.prepareStatement("SELECT COUNT(*) FROM $table").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next())
                result.getInt(1)
            }
        }
        count("cms_list_item") to count("cms_advertisement")
    }

private fun requireRuntimeCompositionEnv(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")
