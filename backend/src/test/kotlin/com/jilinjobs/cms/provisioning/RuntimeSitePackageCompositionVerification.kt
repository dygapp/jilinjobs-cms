package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.flywaydb.core.Flyway
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Path
import java.sql.DriverManager

fun main() {
    val dbUrl = requireEu40Env("SITE_PACKAGE_VERIFY_DB_URL")
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

    val operationalBefore = operationalCounts(dbUrl, dbUsername, dbPassword)
    require(countCodedPresetNavigation(dbUrl, dbUsername, dbPassword) == 0) {
        "Legacy V2 baseline 在 Site Package runtime composition 前不应已有 navigation stable code"
    }

    val first = startEu40Context(dbUrl, dbUsername, dbPassword, packageRoot)
    try {
        val composition = first.getBean(SitePackageRuntimeComposition::class.java)
        require(composition.packageRoot == packageRoot)
        require(composition.report.created == 0 && composition.report.updated == 40 && composition.report.unchanged == 58) {
            "首次 Runtime composition 应原位 adoption 40 条 Legacy navigation：${composition.report}"
        }
        require(composition.report.objects == 98)
        require(countCodedPresetNavigation(dbUrl, dbUsername, dbPassword) == 40)
        require(operationalCounts(dbUrl, dbUsername, dbPassword) == operationalBefore) {
            "Runtime composition 不得改写 V2 operational seed"
        }
    } finally {
        first.close()
    }

    val second = startEu40Context(dbUrl, dbUsername, dbPassword, packageRoot)
    try {
        val composition = second.getBean(SitePackageRuntimeComposition::class.java)
        require(composition.report.created == 0 && composition.report.updated == 0 && composition.report.unchanged == 98) {
            "第二次 Runtime composition 必须幂等：${composition.report}"
        }
        require(countCodedPresetNavigation(dbUrl, dbUsername, dbPassword) == 40)
        require(operationalCounts(dbUrl, dbUsername, dbPassword) == operationalBefore)
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
    } finally {
        disabled.close()
    }

    println("EU40_RUNTIME_SITE_PACKAGE_COMPOSITION_VERIFY PASS")
}

private fun startEu40Context(
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

private fun requireEu40Env(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")
