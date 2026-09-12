package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.flywaydb.core.Flyway
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Path
import java.sql.DriverManager

private val EXPECTED_MAIN_LIST_ITEM_COUNTS = mapOf(
    "HOME_CAROUSEL" to 1,
    "SITE_RELATED" to 5,
    "SITE_REGIONAL_GRADUATES" to 31,
    "SITE_JILIN_UNIVERSITIES" to 60,
)

fun main() {
    val dbUrl = requireBootstrapEnv("SITE_PACKAGE_VERIFY_DB_URL")
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

    verifyGenericMigrationHistory(dbUrl, dbUsername, dbPassword)
    require(bootstrapOperationalCounts(dbUrl, dbUsername, dbPassword) == (0 to 0)) {
        "Generic CMS schema migration 后 operational defaults 必须为空"
    }

    val first = startBootstrapContext(dbUrl, dbUsername, dbPassword, packageRoot, bootstrapOnStart = true)
    try {
        val composition = first.getBean(SitePackageRuntimeComposition::class.java)
        val bootstrap = first.getBean(SitePackageBootstrapRuntime::class.java)
        require(composition.report.created == 98 && composition.report.updated == 0 && composition.report.unchanged == 0) {
            "Fresh install stable structure 结果异常：${composition.report}"
        }
        require(bootstrap.report.status == "APPLIED") { "Fresh install bootstrap 必须执行一次：${bootstrap.report}" }
        require(bootstrapOperationalCounts(dbUrl, dbUsername, dbPassword) == (97 to 1)) {
            "Fresh install 必须建立 97 条 Main ListItem + 1 条 Advertisement"
        }
        require(mainListItemCounts(dbUrl, dbUsername, dbPassword) == EXPECTED_MAIN_LIST_ITEM_COUNTS) {
            "Fresh install Main ListItem 分组数量异常：${mainListItemCounts(dbUrl, dbUsername, dbPassword)}"
        }
        require(listItemCount(dbUrl, dbUsername, dbPassword, "PARTY_CAROUSEL") == 0) {
            "Main bootstrap 不得写入 PARTY_CAROUSEL"
        }
        require(bootstrapStateCount(dbUrl, dbUsername, dbPassword) == 1) { "Bootstrap completion state 必须记录一次" }
    } finally {
        first.close()
    }

    val repeated = startBootstrapContext(dbUrl, dbUsername, dbPassword, packageRoot, bootstrapOnStart = true)
    try {
        val composition = repeated.getBean(SitePackageRuntimeComposition::class.java)
        val bootstrap = repeated.getBean(SitePackageBootstrapRuntime::class.java)
        require(composition.report.created == 0 && composition.report.updated == 0 && composition.report.unchanged == 98) {
            "Repeated stable structure reconcile 必须幂等：${composition.report}"
        }
        require(bootstrap.report.status == "ALREADY_APPLIED") { "Repeated bootstrap 必须由 completion state 拦截：${bootstrap.report}" }
        require(bootstrapOperationalCounts(dbUrl, dbUsername, dbPassword) == (97 to 1)) { "Repeated bootstrap 不得复制运营数据" }
        require(mainListItemCounts(dbUrl, dbUsername, dbPassword) == EXPECTED_MAIN_LIST_ITEM_COUNTS) {
            "Repeated bootstrap 不得改变 Main ListItem 分组数量"
        }
        require(listItemCount(dbUrl, dbUsername, dbPassword, "PARTY_CAROUSEL") == 0)
        require(bootstrapStateCount(dbUrl, dbUsername, dbPassword) == 1)
    } finally {
        repeated.close()
    }

    mutateOperationalDefaults(dbUrl, dbUsername, dbPassword)
    require(bootstrapOperationalCounts(dbUrl, dbUsername, dbPassword) == (96 to 1))
    require(mainListItemCounts(dbUrl, dbUsername, dbPassword) == EXPECTED_MAIN_LIST_ITEM_COUNTS + ("SITE_RELATED" to 4))
    require(advertisementTitle(dbUrl, dbUsername, dbPassword) == "运营已修改标题")

    val ordinaryRuntime = startBootstrapContext(dbUrl, dbUsername, dbPassword, packageRoot, bootstrapOnStart = false)
    try {
        require(ordinaryRuntime.getBeansOfType(SitePackageBootstrapRuntime::class.java).isEmpty()) {
            "普通 Runtime composition 不得执行 bootstrap"
        }
        require(bootstrapOperationalCounts(dbUrl, dbUsername, dbPassword) == (96 to 1)) { "普通 Runtime 不得复活已删除运营数据" }
        require(mainListItemCounts(dbUrl, dbUsername, dbPassword) == EXPECTED_MAIN_LIST_ITEM_COUNTS + ("SITE_RELATED" to 4))
        require(advertisementTitle(dbUrl, dbUsername, dbPassword) == "运营已修改标题") { "普通 Runtime 不得覆盖运营修改" }
    } finally {
        ordinaryRuntime.close()
    }

    val guardedRepeat = startBootstrapContext(dbUrl, dbUsername, dbPassword, packageRoot, bootstrapOnStart = true)
    try {
        val bootstrap = guardedRepeat.getBean(SitePackageBootstrapRuntime::class.java)
        require(bootstrap.report.status == "ALREADY_APPLIED")
        require(bootstrapOperationalCounts(dbUrl, dbUsername, dbPassword) == (96 to 1)) {
            "即使再次显式请求 bootstrap，也不得复活已删除运营数据"
        }
        require(mainListItemCounts(dbUrl, dbUsername, dbPassword) == EXPECTED_MAIN_LIST_ITEM_COUNTS + ("SITE_RELATED" to 4))
        require(advertisementTitle(dbUrl, dbUsername, dbPassword) == "运营已修改标题") {
            "Bootstrap completion state 必须保护运营修改"
        }
    } finally {
        guardedRepeat.close()
    }

    println("EU53_MAIN_LISTITEM_BOOTSTRAP_VERIFY PASS")
}

private fun startBootstrapContext(
    dbUrl: String,
    username: String,
    password: String,
    packageRoot: Path,
    bootstrapOnStart: Boolean,
) = SpringApplicationBuilder(CmsApplication::class.java)
    .web(WebApplicationType.NONE)
    .run(
        "--spring.datasource.url=$dbUrl",
        "--spring.datasource.username=$username",
        "--spring.datasource.password=$password",
        "--cms.site-package.root=$packageRoot",
        "--cms.site-package.bootstrap-on-start=$bootstrapOnStart",
        "--spring.main.banner-mode=off",
    )

private fun verifyGenericMigrationHistory(dbUrl: String, username: String, password: String) {
    DriverManager.getConnection(dbUrl, username, password).use { connection ->
        connection.prepareStatement("SELECT version,description FROM flyway_schema_history WHERE success=1 AND type='SQL' ORDER BY installed_rank").use { statement ->
            statement.executeQuery().use { result ->
                val migrations = buildList {
                    while (result.next()) add(result.getString("version") to result.getString("description"))
                }
                require(
                    migrations == listOf(
                        "1" to "current cms schema",
                        "2" to "site provisioning schema capabilities",
                        "3" to "page content migration mapping",
                        "4" to "page content architecture",
                    ),
                ) {
                    "Backend Flyway history 必须只包含 accepted Generic CMS schema migrations：$migrations"
                }
            }
        }
    }
}

private fun mutateOperationalDefaults(dbUrl: String, username: String, password: String) {
    DriverManager.getConnection(dbUrl, username, password).use { connection ->
        connection.prepareStatement(
            "DELETE i FROM cms_list_item i JOIN cms_list l ON l.id=i.list_id WHERE l.code='SITE_RELATED' AND i.title='全国征兵网'",
        ).use { statement -> require(statement.executeUpdate() == 1) }
        connection.prepareStatement(
            "UPDATE cms_advertisement a JOIN cms_ad_slot s ON s.id=a.slot_id SET a.title='运营已修改标题' WHERE s.code='HOME_RECRUITMENT_PROMO'",
        ).use { statement -> require(statement.executeUpdate() == 1) }
    }
}

private fun bootstrapOperationalCounts(dbUrl: String, username: String, password: String): Pair<Int, Int> =
    DriverManager.getConnection(dbUrl, username, password).use { connection ->
        fun count(table: String): Int = connection.prepareStatement("SELECT COUNT(*) FROM $table").use { statement ->
            statement.executeQuery().use { result -> require(result.next()); result.getInt(1) }
        }
        count("cms_list_item") to count("cms_advertisement")
    }

private fun mainListItemCounts(dbUrl: String, username: String, password: String): Map<String, Int> =
    EXPECTED_MAIN_LIST_ITEM_COUNTS.keys.associateWith { code -> listItemCount(dbUrl, dbUsername, dbPassword, code) }

private fun listItemCount(dbUrl: String, username: String, password: String, listCode: String): Int =
    DriverManager.getConnection(dbUrl, username, password).use { connection ->
        connection.prepareStatement(
            "SELECT COUNT(*) FROM cms_list_item i JOIN cms_list l ON l.id=i.list_id WHERE l.code=?",
        ).use { statement ->
            statement.setString(1, listCode)
            statement.executeQuery().use { result -> require(result.next()); result.getInt(1) }
        }
    }

private fun bootstrapStateCount(dbUrl: String, username: String, password: String): Int =
    DriverManager.getConnection(dbUrl, username, password).use { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM cms_site_bootstrap_state WHERE package_id='jilinjobs' AND bootstrap_id='initial-operational-data'").use { statement ->
            statement.executeQuery().use { result -> require(result.next()); result.getInt(1) }
        }
    }

private fun advertisementTitle(dbUrl: String, username: String, password: String): String =
    DriverManager.getConnection(dbUrl, username, password).use { connection ->
        connection.prepareStatement(
            "SELECT a.title FROM cms_advertisement a JOIN cms_ad_slot s ON s.id=a.slot_id WHERE s.code='HOME_RECRUITMENT_PROMO'",
        ).use { statement ->
            statement.executeQuery().use { result -> require(result.next()); result.getString(1) }
        }
    }

private fun requireBootstrapEnv(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")