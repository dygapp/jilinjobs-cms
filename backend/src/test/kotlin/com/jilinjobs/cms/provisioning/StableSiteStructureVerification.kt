package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.Connection
import javax.sql.DataSource

fun main() {
    val dbUrl = requireEu38Env("SITE_PACKAGE_VERIFY_DB_URL")
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

        resetDatabase(dataSource, target = "1")
        val definition = loader.load(packageRoot)
        require(definition.manifest.packageId == "jilinjobs")
        require(definition.objectCount == 58) { "JilinJobs structural package object count 异常：${definition.objectCount}" }
        require(definition.columns.size == 18)
        require(definition.pageGroups.size == 2)
        require(definition.pages.size == 16)
        require(definition.navigationLocations.size == 3)
        require(definition.siteConfig.size == 13)
        require(definition.lists.size == 5)
        require(definition.advertisementSlots.size == 1)

        insertOperatorData(dataSource)
        val first = provisioner.apply(packageRoot)
        require(first.created == 58 && first.updated == 0 && first.unchanged == 0) {
            "Fresh V1 first apply 结果异常：$first"
        }
        val freshSnapshot = structuralSnapshot(dataSource)
        val second = provisioner.apply(packageRoot)
        require(second.created == 0 && second.updated == 0 && second.unchanged == 58) {
            "Fresh V1 second apply 必须幂等：$second"
        }
        verifyOperatorDataUnchanged(dataSource)

        val update = provisioner.apply(createSiteConfigUpdatePackage("吉林就业-EU38验证"))
        require(update.created == 0 && update.updated == 1 && update.unchanged == 0) {
            "SiteConfig representative reconcile 结果异常：$update"
        }
        require(siteConfigValue(dataSource, "SITE_SHORT_NAME") == "吉林就业-EU38验证")
        val restore = provisioner.apply(packageRoot)
        require(restore.created == 0 && restore.updated == 1 && restore.unchanged == 57) {
            "正式 package 应恢复 representative update：$restore"
        }
        require(structuralSnapshot(dataSource) == freshSnapshot) { "恢复正式 package 后 structural snapshot 漂移" }

        expectEu38Validation("new-domain ownership conflict") {
            provisioner.apply(createListConflictPackage())
        }

        resetDatabase(dataSource, target = null)
        require(countRows(dataSource, "cms_navigation") > 0) { "Legacy V2 未建立 navigation compatibility snapshot" }
        require(countRows(dataSource, "cms_list_item") > 0) { "Legacy V2 未保留 operational list members" }
        require(countRows(dataSource, "cms_advertisement") > 0) { "Legacy V2 未保留 operational advertisements" }
        val legacyOperationalBefore = operationalSnapshot(dataSource)
        insertLegacyOperatorData(dataSource)

        val legacyApply = provisioner.apply(packageRoot)
        require(legacyApply.created == 0 && legacyApply.updated == 0 && legacyApply.unchanged == 58) {
            "V1+V2 compatibility apply 应安全 adopt 当前 preset baseline：$legacyApply"
        }
        val legacySecond = provisioner.apply(packageRoot)
        require(legacySecond.created == 0 && legacySecond.updated == 0 && legacySecond.unchanged == 58) {
            "Legacy second apply 必须幂等：$legacySecond"
        }
        require(structuralSnapshot(dataSource) == freshSnapshot) {
            "V1-only + Site Package 与 V1+V2 + Site Package structural state 不等价"
        }
        require(operationalSnapshot(dataSource) == legacyOperationalBefore) {
            "Site Package 不得改写 V2 operational navigation/list/ad members"
        }
        verifyLegacyOperatorDataUnchanged(dataSource)

        println("EU38_STABLE_SITE_STRUCTURE_VERIFY PASS")
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

private fun createSiteConfigUpdatePackage(value: String): Path {
    val root = Files.createTempDirectory("eu38-config-update-")
    val structure = root.resolve("structure")
    Files.createDirectories(structure)
    val config = structure.resolve("site-config.json")
    Files.writeString(
        config,
        """
        [
          {
            "key": "SITE_SHORT_NAME",
            "propertyName": "网站简称",
            "groupCode": "BASIC",
            "value": "$value",
            "valueType": "TEXT",
            "description": "网站简称",
            "sortOrder": 20,
            "required": false,
            "systemFlag": true,
            "enabled": true,
            "preset": true
          }
        ]
        """.trimIndent() + "\n",
    )
    writeEu38Manifest(root, "site-config", "structure/site-config.json", sha256Eu38(config))
    return root
}

private fun createListConflictPackage(): Path {
    val root = Files.createTempDirectory("eu38-list-conflict-")
    val structure = root.resolve("structure")
    Files.createDirectories(structure)
    val lists = structure.resolve("lists.json")
    Files.writeString(
        lists,
        """
        [
          {
            "code": "OPERATOR_LIST",
            "name": "Attempted takeover",
            "groupCode": "GENERAL",
            "imagePolicy": "OPTIONAL",
            "description": "",
            "sortOrder": 1,
            "enabled": true,
            "systemFlag": false,
            "preset": true
          }
        ]
        """.trimIndent() + "\n",
    )
    writeEu38Manifest(root, "lists", "structure/lists.json", sha256Eu38(lists))
    return root
}

private fun writeEu38Manifest(root: Path, type: String, path: String, digest: String) {
    Files.writeString(
        root.resolve("manifest.json"),
        """{"packageId":"eu38-verifier","schemaVersion":1,"version":"1.0.0","structure":[{"type":"$type","path":"$path","sha256":"$digest"}]}""" + "\n",
    )
}

private fun insertOperatorData(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "INSERT INTO cms_column(parent_id,alias,name,cover_policy,sort_order,enabled,preset) VALUES(NULL,'operator-owned-eu38','运营栏目 EU38','OPTIONAL',900,1,0)",
        ).use { it.executeUpdate() }
        connection.prepareStatement(
            "INSERT INTO cms_list(code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset) VALUES('OPERATOR_LIST','运营列表','GENERAL','OPTIONAL','',900,1,0,0)",
        ).use { it.executeUpdate() }
    }
}

private fun verifyOperatorDataUnchanged(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT name,preset FROM cms_column WHERE alias='operator-owned-eu38'").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next() && result.getString("name") == "运营栏目 EU38" && !result.getBoolean("preset"))
            }
        }
        connection.prepareStatement("SELECT name,preset FROM cms_list WHERE code='OPERATOR_LIST'").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next() && result.getString("name") == "运营列表" && !result.getBoolean("preset"))
            }
        }
    }
}

private fun insertLegacyOperatorData(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "INSERT INTO cms_column(parent_id,alias,name,cover_policy,sort_order,enabled,preset) VALUES(NULL,'legacy-operator-eu38','Legacy 运营栏目','OPTIONAL',901,1,0)",
        ).use { it.executeUpdate() }
    }
}

private fun verifyLegacyOperatorDataUnchanged(dataSource: DataSource) {
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT name,preset FROM cms_column WHERE alias='legacy-operator-eu38'").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next() && result.getString("name") == "Legacy 运营栏目" && !result.getBoolean("preset"))
            }
        }
    }
}

private fun structuralSnapshot(dataSource: DataSource): String {
    val queries = listOf(
        """SELECT c.alias,c.name,COALESCE(p.alias,''),c.cover_policy,c.sort_order,c.enabled,c.preset FROM cms_column c LEFT JOIN cms_column p ON p.id=c.parent_id WHERE c.preset=1 ORDER BY c.alias""",
        "SELECT alias,name,sort_order,enabled,preset FROM cms_page_group WHERE preset=1 ORDER BY alias",
        """SELECT COALESCE(g.alias,''),p.alias,p.name,p.body_html,p.render_mode,COALESCE(p.embed_url,''),p.sort_order,p.enabled,p.preset FROM cms_page p LEFT JOIN cms_page_group g ON g.id=p.group_id WHERE p.preset=1 ORDER BY COALESCE(g.alias,''),p.alias""",
        "SELECT code,name,description,sort_order,enabled,system_flag,preset FROM cms_navigation_location WHERE preset=1 ORDER BY code",
        """SELECT config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system_flag,enabled,preset FROM cms_site_config WHERE preset=1 ORDER BY config_key""",
        "SELECT code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset FROM cms_list WHERE preset=1 ORDER BY code",
        "SELECT code,name,description,sort_order,enabled,system_flag,preset FROM cms_ad_slot WHERE preset=1 ORDER BY code",
    )
    return dataSource.connection.use { connection -> queries.joinToString("\n--domain--\n") { canonicalQuery(connection, it) } }
}

private fun operationalSnapshot(dataSource: DataSource): String {
    val queries = listOf(
        """SELECT n.name,n.position,COALESCE(p.name,''),n.target_type,COALESCE(c.alias,''),COALESCE(pg.alias,''),COALESCE(n.target_url,''),n.open_mode,COALESCE(n.icon_path,''),n.sort_order,n.enabled,n.preset FROM cms_navigation n LEFT JOIN cms_navigation p ON p.id=n.parent_id LEFT JOIN cms_column c ON c.id=n.target_column_id LEFT JOIN cms_page pg ON pg.id=n.target_page_id ORDER BY n.id""",
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

private fun siteConfigValue(dataSource: DataSource, key: String): String? =
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT config_value FROM cms_site_config WHERE config_key=?").use { statement ->
            statement.setString(1, key)
            statement.executeQuery().use { result -> if (result.next()) result.getString(1) else null }
        }
    }

private fun countRows(dataSource: DataSource, table: String): Int =
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM $table").use { statement ->
            statement.executeQuery().use { result -> require(result.next()); result.getInt(1) }
        }
    }

private fun expectEu38Validation(label: String, action: () -> Unit) {
    val error = runCatching(action).exceptionOrNull()
    require(error is SitePackageValidationException) {
        "$label 应被 SitePackageValidationException 拒绝，实际：${error?.javaClass?.name}"
    }
}

private fun requireEu38Env(name: String): String = System.getenv(name)?.takeIf { it.isNotBlank() }
    ?: error("缺少验证环境变量：$name")

private fun sha256Eu38(path: Path): String {
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
