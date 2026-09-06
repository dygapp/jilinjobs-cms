package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Path
import java.sql.Connection
import javax.sql.DataSource

fun main() {
    val dbUrl = requireEu39Env("SITE_PACKAGE_VERIFY_DB_URL")
    val dbUsername = System.getenv("SITE_PACKAGE_VERIFY_DB_USERNAME") ?: "root"
    val dbPassword = System.getenv("SITE_PACKAGE_VERIFY_DB_PASSWORD") ?: "root"
    val context = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=$dbUsername",
            "--spring.datasource.password=$dbPassword",
            "--spring.flyway.target=1",
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

        // Fresh Generic Schema path intentionally excludes V2 site bootstrap but includes V3 Core schema evolution.
        resetDatabase(dataSource, target = "1")
        applyNavigationIdentitySchema(dataSource)
        insertOperatorNavigation(dataSource, "fresh-operator")
        val freshFirst = provisioner.apply(packageRoot)
        require(freshFirst.created == 98 && freshFirst.updated == 0 && freshFirst.unchanged == 0) {
            "Fresh V1+V3 first apply 结果异常：$freshFirst"
        }
        val freshSnapshot = structuralSnapshot(dataSource)
        val freshSecond = provisioner.apply(packageRoot)
        require(freshSecond.created == 0 && freshSecond.updated == 0 && freshSecond.unchanged == 98) {
            "Fresh second apply 必须幂等：$freshSecond"
        }
        verifyOperatorNavigation(dataSource, "fresh-operator")

        // Legacy V1+V2+V3 path adopts the current preset tree in place instead of duplicating it.
        resetDatabase(dataSource, target = null)
        val legacyNavigationCount = countRows(dataSource, "cms_navigation")
        require(legacyNavigationCount == 40) { "Legacy V2 navigation baseline 数量异常：$legacyNavigationCount" }
        val operationalBefore = operationalSnapshot(dataSource)
        insertOperatorNavigation(dataSource, "legacy-operator")
        val legacyApply = provisioner.apply(packageRoot)
        require(legacyApply.created == 0 && legacyApply.updated == 40 && legacyApply.unchanged == 58) {
            "Legacy stable-code adoption 结果异常：$legacyApply"
        }
        require(countRows(dataSource, "cms_navigation") == legacyNavigationCount + 1) { "Legacy adoption 不得复制导航树" }
        require(countCodedPresetNavigations(dataSource) == 40) { "Legacy preset navigation 必须全部获得 stable code" }
        val legacySecond = provisioner.apply(packageRoot)
        require(legacySecond.created == 0 && legacySecond.updated == 0 && legacySecond.unchanged == 98) {
            "Legacy second apply 必须幂等：$legacySecond"
        }
        require(structuralSnapshot(dataSource) == freshSnapshot) {
            "Fresh Generic Schema + Site Package 与 Legacy V1+V2+V3 + Site Package structural state 不等价"
        }
        require(operationalSnapshot(dataSource) == operationalBefore) { "Navigation ownership 收敛不得改写 ListItem / Advertisement 运营成员" }
        verifyOperatorNavigation(dataSource, "legacy-operator")

        // Once adopted, rename / move / reorder / retarget are reconciled by code rather than mutable fields.
        mutateStableNavigation(dataSource)
        val restore = provisioner.apply(packageRoot)
        require(restore.created == 0 && restore.updated == 1 && restore.unchanged == 97) {
            "Stable-code navigation restore 结果异常：$restore"
        }
        verifyRestoredNavigation(dataSource)

        // A pre-identity Legacy preset that no longer matches the accepted baseline fails safely instead of duplicating.
        resetDatabase(dataSource, target = null)
        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE cms_navigation SET name='Legacy 已人工改名' WHERE parent_id IS NULL AND position='MAIN' AND name='网站首页' AND preset=1").use { it.executeUpdate() }
        }
        val beforeAmbiguousApply = countRows(dataSource, "cms_navigation")
        expectEu39Validation("ambiguous legacy navigation adoption") { provisioner.apply(packageRoot) }
        require(countRows(dataSource, "cms_navigation") == beforeAmbiguousApply) { "Ambiguous adoption 失败后不得新增导航" }
        require(countCodedPresetNavigations(dataSource) == 0) { "Ambiguous adoption 必须整体回滚" }

        println("EU39_NAVIGATION_STABLE_IDENTITY_VERIFY PASS")
    } finally {
        context.close()
    }
}

private fun resetDatabase(dataSource: DataSource, target: String?) {
    val configuration = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .cleanDisabled(false)
    if (target != null) configuration.target(MigrationVersion.fromVersion(target))
    val flyway = configuration.load()
    flyway.clean()
    flyway.migrate()
}

private fun applyNavigationIdentitySchema(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.execute("ALTER TABLE cms_navigation ADD COLUMN code VARCHAR(100) NULL AFTER id")
            statement.execute("CREATE UNIQUE INDEX uk_cms_navigation_code ON cms_navigation(code)")
        }
    }
}

private fun insertOperatorNavigation(dataSource: DataSource, name: String) {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "INSERT INTO cms_navigation(code,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,icon_path,sort_order,enabled,preset) VALUES(NULL,NULL,?,'MAIN',NULL,'LINK',NULL,NULL,'https://operator.invalid/','DEFAULT',NULL,999,1,0)",
        ).use { statement -> statement.setString(1, name); statement.executeUpdate() }
    }
}

private fun verifyOperatorNavigation(dataSource: DataSource, name: String) {
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

private fun verifyRestoredNavigation(dataSource: DataSource) {
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
        connection.prepareStatement("SELECT COUNT(*) FROM cms_navigation WHERE code='main-policy'").use { statement ->
            statement.executeQuery().use { result -> require(result.next() && result.getInt(1) == 1) }
        }
    }
}

private fun structuralSnapshot(dataSource: DataSource): String {
    val queries = listOf(
        """SELECT c.alias,c.name,COALESCE(p.alias,''),c.cover_policy,c.sort_order,c.enabled,c.preset FROM cms_column c LEFT JOIN cms_column p ON p.id=c.parent_id WHERE c.preset=1 ORDER BY c.alias""",
        "SELECT alias,name,sort_order,enabled,preset FROM cms_page_group WHERE preset=1 ORDER BY alias",
        """SELECT COALESCE(g.alias,''),p.alias,p.name,p.body_html,p.render_mode,COALESCE(p.embed_url,''),p.sort_order,p.enabled,p.preset FROM cms_page p LEFT JOIN cms_page_group g ON g.id=p.group_id WHERE p.preset=1 ORDER BY COALESCE(g.alias,''),p.alias""",
        "SELECT code,name,description,sort_order,enabled,system_flag,preset FROM cms_navigation_location WHERE preset=1 ORDER BY code",
        """SELECT n.code,n.name,n.position,COALESCE(p.code,''),n.target_type,COALESCE(c.alias,''),COALESCE(g.alias,''),COALESCE(pg.alias,''),COALESCE(n.target_url,''),n.open_mode,COALESCE(n.icon_path,''),n.sort_order,n.enabled,n.preset FROM cms_navigation n LEFT JOIN cms_navigation p ON p.id=n.parent_id LEFT JOIN cms_column c ON c.id=n.target_column_id LEFT JOIN cms_page pg ON pg.id=n.target_page_id LEFT JOIN cms_page_group g ON g.id=pg.group_id WHERE n.preset=1 ORDER BY n.code""",
        """SELECT config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system_flag,enabled,preset FROM cms_site_config WHERE preset=1 ORDER BY config_key""",
        "SELECT code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset FROM cms_list WHERE preset=1 ORDER BY code",
        "SELECT code,name,description,sort_order,enabled,system_flag,preset FROM cms_ad_slot WHERE preset=1 ORDER BY code",
    )
    return dataSource.connection.use { connection -> queries.joinToString("\n--domain--\n") { canonicalQuery(connection, it) } }
}

private fun operationalSnapshot(dataSource: DataSource): String {
    val queries = listOf(
        """SELECT l.code,i.source_type,i.title,COALESCE(i.url,''),COALESCE(i.image_path,''),i.open_mode,i.sort_order,i.enabled FROM cms_list_item i JOIN cms_list l ON l.id=i.list_id ORDER BY i.id""",
        """SELECT s.code,a.title,a.image_path,COALESCE(a.url,''),a.open_mode,a.sort_order,a.enabled FROM cms_advertisement a JOIN cms_ad_slot s ON s.id=a.slot_id ORDER BY a.id""",
    )
    return dataSource.connection.use { connection -> queries.joinToString("\n--operational--\n") { canonicalQuery(connection, it) } }
}

private fun canonicalQuery(connection: Connection, sql: String): String =
    connection.prepareStatement(sql).use { statement ->
        statement.executeQuery().use { result ->
            val count = result.metaData.columnCount
            buildString {
                while (result.next()) {
                    for (index in 1..count) {
                        if (index > 1) append('\u001f')
                        append(result.getString(index) ?: "<null>")
                    }
                    append('\n')
                }
            }
        }
    }

private fun countRows(dataSource: DataSource, table: String): Int =
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM $table").use { statement ->
            statement.executeQuery().use { result -> require(result.next()); result.getInt(1) }
        }
    }

private fun countCodedPresetNavigations(dataSource: DataSource): Int =
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM cms_navigation WHERE preset=1 AND code IS NOT NULL").use { statement ->
            statement.executeQuery().use { result -> require(result.next()); result.getInt(1) }
        }
    }

private fun expectEu39Validation(label: String, action: () -> Unit) {
    val error = runCatching(action).exceptionOrNull()
    require(error is SitePackageValidationException) {
        "$label 应被 SitePackageValidationException 拒绝，实际：${error?.javaClass?.name}"
    }
}

private fun requireEu39Env(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")
