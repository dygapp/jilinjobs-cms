package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.flywaydb.core.Flyway
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Path
import javax.sql.DataSource

fun main() {
    val dbUrl = requireStableStructureEnv("SITE_PACKAGE_VERIFY_DB_URL")
    val dbUsername = System.getenv("SITE_PACKAGE_VERIFY_DB_USERNAME") ?: "root"
    val dbPassword = System.getenv("SITE_PACKAGE_VERIFY_DB_PASSWORD") ?: "root"
    val context = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=$dbUsername",
            "--spring.datasource.password=$dbPassword",
            "--spring.main.banner-mode=off",
        )
    try {
        val dataSource = context.getBean(DataSource::class.java)
        val loader = context.getBean(SitePackageLoader::class.java)
        val provisioner = context.getBean(SitePackageProvisioner::class.java)
        val packageRoot = Path.of("../sites/jilinjobs").toAbsolutePath().normalize()
        val definition = loader.load(packageRoot)

        require(definition.manifest.packageId == "jilinjobs")
        require(definition.objectCount == 98) { "JilinJobs Site Package object count 异常：${definition.objectCount}" }
        require(definition.navigationItems.size == 40) { "NavigationItem package count 异常" }

        resetStableStructureDatabase(dataSource)
        require(countStableStructureRows(dataSource, "cms_column") == 0) { "Generic CMS Flyway 不得内建 JilinJobs Column" }
        require(countStableStructureRows(dataSource, "cms_navigation") == 0) { "Generic CMS Flyway 不得内建 JilinJobs Navigation" }
        require(countStableStructureRows(dataSource, "cms_list") == 0) { "Generic CMS Flyway 不得内建 JilinJobs CmsList" }
        require(countStableStructureRows(dataSource, "cms_ad_slot") == 0) { "Generic CMS Flyway 不得内建 JilinJobs AdvertisementSlot" }
        require(countStableStructureRows(dataSource, "cms_list_item") == 0 && countStableStructureRows(dataSource, "cms_advertisement") == 0) {
            "Generic CMS Flyway 不得内建 Site operational defaults"
        }

        val freshFirst = provisioner.apply(packageRoot)
        require(freshFirst.created == 98 && freshFirst.updated == 0 && freshFirst.unchanged == 0) {
            "Fresh Generic Schema first Site Package apply 结果异常：$freshFirst"
        }
        require(countCodedPresetNavigations(dataSource) == 40) { "Fresh Site Package 必须直接建立 40 条 coded preset navigation" }

        insertStableStructureOperatorNavigation(dataSource, "fresh-operator")
        val freshSecond = provisioner.apply(packageRoot)
        require(freshSecond.created == 0 && freshSecond.updated == 0 && freshSecond.unchanged == 98) {
            "Fresh second apply 必须幂等：$freshSecond"
        }
        verifyStableStructureOperatorNavigation(dataSource, "fresh-operator")

        mutateStableNavigation(dataSource)
        val restore = provisioner.apply(packageRoot)
        require(restore.created == 0 && restore.updated == 1 && restore.unchanged == 97) {
            "Stable-code navigation restore 结果异常：$restore"
        }
        verifyRestoredStableNavigation(dataSource)
        verifyStableStructureOperatorNavigation(dataSource, "fresh-operator")

        require(countStableStructureRows(dataSource, "cms_list_item") == 0 && countStableStructureRows(dataSource, "cms_advertisement") == 0) {
            "Stable structure reconcile 不得隐式创建 operational defaults"
        }

        println("EU41_STABLE_SITE_STRUCTURE_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun resetStableStructureDatabase(dataSource: DataSource) {
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .cleanDisabled(false)
        .load()
    flyway.clean()
    flyway.migrate()
}

private fun insertStableStructureOperatorNavigation(dataSource: DataSource, name: String) {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "INSERT INTO cms_navigation(code,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,icon_path,sort_order,enabled,preset) VALUES(NULL,NULL,?,'MAIN',NULL,'LINK',NULL,NULL,'https://operator.invalid/','DEFAULT',NULL,999,1,0)",
        ).use { statement ->
            statement.setString(1, name)
            statement.executeUpdate()
        }
    }
}

private fun verifyStableStructureOperatorNavigation(dataSource: DataSource, name: String) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT code,target_url,preset FROM cms_navigation WHERE name=?").use { statement ->
            statement.setString(1, name)
            statement.executeQuery().use { result ->
                require(result.next())
                require(result.getString("code") == null)
                require(result.getString("target_url") == "https://operator.invalid/")
                require(!result.getBoolean("preset"))
            }
        }
    }
}

private fun mutateStableNavigation(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "UPDATE cms_navigation SET parent_id=(SELECT p.id FROM (SELECT id FROM cms_navigation WHERE code='main-guide') p),name='已改名',position='HOME_QUICK',target_type='LINK',target_column_id=NULL,target_url='https://changed.invalid/',sort_order=999 WHERE code='main-policy'",
        ).use { it.executeUpdate() }
    }
}

private fun verifyRestoredStableNavigation(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "SELECT n.name,n.position,n.parent_id,n.target_type,c.alias,n.target_url,n.sort_order,n.preset FROM cms_navigation n LEFT JOIN cms_column c ON c.id=n.target_column_id WHERE n.code='main-policy'",
        ).use { statement ->
            statement.executeQuery().use { result ->
                require(result.next())
                require(result.getString("name") == "政策法规")
                require(result.getString("position") == "MAIN")
                require(result.getObject("parent_id") == null)
                require(result.getString("target_type") == "COLUMN")
                require(result.getString("alias") == "policy")
                require(result.getString("target_url") == null)
                require(result.getInt("sort_order") == 50)
                require(result.getBoolean("preset"))
            }
        }
    }
}

private fun countStableStructureRows(dataSource: DataSource, table: String): Int =
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM $table").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next())
                result.getInt(1)
            }
        }
    }

private fun countCodedPresetNavigations(dataSource: DataSource): Int =
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM cms_navigation WHERE preset=1 AND code IS NOT NULL").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next())
                result.getInt(1)
            }
        }
    }

private fun requireStableStructureEnv(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")
