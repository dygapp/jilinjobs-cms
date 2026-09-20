package com.jilinjobs.cms.migration

import java.sql.Connection
import java.sql.DriverManager
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion

fun main() {
    val dbUrl = requireLinkOpenModeEnv("SITE_PACKAGE_VERIFY_DB_URL")
    val username = System.getenv("SITE_PACKAGE_VERIFY_DB_USERNAME") ?: "root"
    val password = System.getenv("SITE_PACKAGE_VERIFY_DB_PASSWORD") ?: "root"

    val v4 = Flyway.configure()
        .dataSource(dbUrl, username, password)
        .locations("classpath:db/migration")
        .target(MigrationVersion.fromVersion("4"))
        .cleanDisabled(false)
        .load()
    v4.clean()
    v4.migrate()

    DriverManager.getConnection(dbUrl, username, password).use(::seedLegacyOpenModes)

    Flyway.configure()
        .dataSource(dbUrl, username, password)
        .locations("classpath:db/migration")
        .target(MigrationVersion.fromVersion("5"))
        .load()
        .migrate()

    DriverManager.getConnection(dbUrl, username, password).use(::verifyMigratedOpenModes)
    println("EU60_LINK_OPEN_MODE_MIGRATION_VERIFY PASS")
}

private fun seedLegacyOpenModes(connection: Connection) {
    connection.createStatement().use { sql ->
        sql.executeUpdate("""
            INSERT INTO cms_navigation_location(code,name,description,sort_order,enabled,system_flag,preset)
            VALUES('MAIN','Main','',0,1,0,0)
        """.trimIndent())
        sql.executeUpdate("""
            INSERT INTO cms_navigation(code,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,icon_path,sort_order,enabled,preset) VALUES
            ('nav-external-default',NULL,'nav-external-default','MAIN',NULL,'LINK',NULL,NULL,'https://example.com/external','DEFAULT',NULL,10,1,0),
            ('nav-internal-default',NULL,'nav-internal-default','MAIN',NULL,'LINK',NULL,NULL,'/internal','DEFAULT',NULL,20,1,0),
            ('nav-new-internal',NULL,'nav-new-internal','MAIN',NULL,'LINK',NULL,NULL,'/new-context','NEW_WINDOW',NULL,30,1,0),
            ('nav-same-external',NULL,'nav-same-external','MAIN',NULL,'LINK',NULL,NULL,'https://example.com/same','SAME_WINDOW',NULL,40,1,0)
        """.trimIndent())

        sql.executeUpdate("""
            INSERT INTO cms_column(alias,name,cover_policy,sort_order,enabled,preset)
            VALUES('migration-open-mode','Migration Open Mode','OPTIONAL',0,1,0)
        """.trimIndent())
        sql.executeUpdate("""
            INSERT INTO cms_article(column_id,title,body_html,source,article_type,external_url,status)
            SELECT id,'external-article','','','EXTERNAL_LINK','https://example.com/article','PUBLISHED'
            FROM cms_column WHERE alias='migration-open-mode'
        """.trimIndent())
        sql.executeUpdate("""
            INSERT INTO cms_article(column_id,title,body_html,source,article_type,external_url,status)
            SELECT id,'internal-article','<p>internal</p>','','INTERNAL',NULL,'PUBLISHED'
            FROM cms_column WHERE alias='migration-open-mode'
        """.trimIndent())

        sql.executeUpdate("""
            INSERT INTO cms_list(code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset)
            VALUES('MIGRATION_OPEN_MODE','Migration Open Mode','GENERAL','OPTIONAL','',0,1,0,0)
        """.trimIndent())
        sql.executeUpdate("""
            INSERT INTO cms_list_item(list_id,source_type,article_id,title,url,open_mode,sort_order,enabled) VALUES
            ((SELECT id FROM cms_list WHERE code='MIGRATION_OPEN_MODE'),'LINK',NULL,'list-external-default','https://example.com/list','DEFAULT',10,1),
            ((SELECT id FROM cms_list WHERE code='MIGRATION_OPEN_MODE'),'LINK',NULL,'list-internal-default','/list-internal','DEFAULT',20,1),
            ((SELECT id FROM cms_list WHERE code='MIGRATION_OPEN_MODE'),'LINK',NULL,'list-new-internal','/list-new','NEW_WINDOW',30,1),
            ((SELECT id FROM cms_list WHERE code='MIGRATION_OPEN_MODE'),'LINK',NULL,'list-same-external','https://example.com/list-same','SAME_WINDOW',40,1),
            ((SELECT id FROM cms_list WHERE code='MIGRATION_OPEN_MODE'),'ARTICLE',(SELECT id FROM cms_article WHERE title='external-article'),'list-article-external-default',NULL,'DEFAULT',50,1),
            ((SELECT id FROM cms_list WHERE code='MIGRATION_OPEN_MODE'),'ARTICLE',(SELECT id FROM cms_article WHERE title='internal-article'),'list-article-internal-default',NULL,'DEFAULT',60,1)
        """.trimIndent())

        sql.executeUpdate("""
            INSERT INTO cms_ad_slot(code,name,description,sort_order,enabled,system_flag,preset)
            VALUES('MIGRATION_OPEN_MODE','Migration Open Mode','',0,1,0,0)
        """.trimIndent())
        sql.executeUpdate("""
            INSERT INTO cms_advertisement(slot_id,title,image_path,url,open_mode,sort_order,enabled) VALUES
            ((SELECT id FROM cms_ad_slot WHERE code='MIGRATION_OPEN_MODE'),'ad-external-default','/static/test.png','https://example.com/ad','DEFAULT',10,1),
            ((SELECT id FROM cms_ad_slot WHERE code='MIGRATION_OPEN_MODE'),'ad-internal-default','/static/test.png','/ad-internal','DEFAULT',20,1),
            ((SELECT id FROM cms_ad_slot WHERE code='MIGRATION_OPEN_MODE'),'ad-new-internal','/static/test.png','/ad-new','NEW_WINDOW',30,1),
            ((SELECT id FROM cms_ad_slot WHERE code='MIGRATION_OPEN_MODE'),'ad-same-external','/static/test.png','https://example.com/ad-same','SAME_WINDOW',40,1),
            ((SELECT id FROM cms_ad_slot WHERE code='MIGRATION_OPEN_MODE'),'ad-no-link','/static/test.png','https://example.com/ad-retained','NO_LINK',50,1)
        """.trimIndent())
    }
}

private fun verifyMigratedOpenModes(connection: Connection) {
    verifyModes(
        connection,
        "SELECT name,open_mode FROM cms_navigation WHERE code LIKE 'nav-%'",
        mapOf(
            "nav-external-default" to "_blank",
            "nav-internal-default" to null,
            "nav-new-internal" to "_blank",
            "nav-same-external" to "_self",
        ),
    )
    verifyModes(
        connection,
        "SELECT title,open_mode FROM cms_list_item WHERE list_id=(SELECT id FROM cms_list WHERE code='MIGRATION_OPEN_MODE')",
        mapOf(
            "list-external-default" to "_blank",
            "list-internal-default" to null,
            "list-new-internal" to "_blank",
            "list-same-external" to "_self",
            "list-article-external-default" to "_blank",
            "list-article-internal-default" to null,
        ),
    )
    verifyModes(
        connection,
        "SELECT title,open_mode FROM cms_advertisement WHERE slot_id=(SELECT id FROM cms_ad_slot WHERE code='MIGRATION_OPEN_MODE')",
        mapOf(
            "ad-external-default" to "_blank",
            "ad-internal-default" to null,
            "ad-new-internal" to "_blank",
            "ad-same-external" to "_self",
            "ad-no-link" to "NO_LINK",
        ),
    )

    for (table in listOf("cms_navigation", "cms_list_item", "cms_advertisement")) {
        connection.prepareStatement("""
            SELECT IS_NULLABLE,COLUMN_DEFAULT
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME='open_mode'
        """.trimIndent()).use { statement ->
            statement.setString(1, table)
            statement.executeQuery().use { result ->
                require(result.next()) { "缺少 $table.open_mode metadata" }
                require(result.getString("IS_NULLABLE") == "YES") { "$table.open_mode 必须允许 NULL" }
                require(result.getString("COLUMN_DEFAULT") == null) { "$table.open_mode 默认值必须为 NULL" }
            }
        }
        connection.prepareStatement("SELECT COUNT(*) FROM $table WHERE open_mode IN ('DEFAULT','SAME_WINDOW','NEW_WINDOW')").use { statement ->
            statement.executeQuery().use { result ->
                require(result.next() && result.getInt(1) == 0) { "$table 仍存在退休 open_mode 值" }
            }
        }
    }

    connection.createStatement().use { sql ->
        sql.executeUpdate("""
            INSERT INTO cms_navigation(code,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,icon_path,sort_order,enabled,preset)
            VALUES('nav-null-default',NULL,'nav-null-default','MAIN',NULL,'LINK',NULL,NULL,'https://example.com/no-inference',NULL,50,1,0)
        """.trimIndent())
    }
    connection.prepareStatement("SELECT open_mode FROM cms_navigation WHERE code='nav-null-default'").use { statement ->
        statement.executeQuery().use { result ->
            require(result.next() && result.getString(1) == null) { "新记录省略 open_mode 时必须保持 NULL" }
        }
    }
}

private fun verifyModes(connection: Connection, sql: String, expected: Map<String, String?>) {
    val actual = linkedMapOf<String, String?>()
    connection.prepareStatement(sql).use { statement ->
        statement.executeQuery().use { result ->
            while (result.next()) actual[result.getString(1)] = result.getString(2)
        }
    }
    require(actual == expected) { "open_mode 迁移结果不一致。expected=$expected actual=$actual" }
}

private fun requireLinkOpenModeEnv(name: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: error("缺少验证环境变量：$name")
