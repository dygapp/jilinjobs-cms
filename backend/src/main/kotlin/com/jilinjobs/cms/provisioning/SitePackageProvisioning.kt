package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.common.ContentImagePolicy
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.Connection
import javax.sql.DataSource

private val PACKAGE_ID = Regex("[a-z0-9][a-z0-9-]{0,63}")
private val COLUMN_ALIAS = Regex("[a-z0-9][a-z0-9-]{0,99}")
private val SHA256 = Regex("[0-9a-f]{64}")
private const val SITE_PACKAGE_SCHEMA_VERSION = 1
private const val COLUMNS_STRUCTURE_TYPE = "columns"

data class SitePackageManifest(
    val packageId: String = "",
    val schemaVersion: Int = 0,
    val version: String = "",
    val structure: List<SitePackageStructureFile> = emptyList(),
)

data class SitePackageStructureFile(
    val type: String = "",
    val path: String = "",
    val sha256: String = "",
)

data class SitePackageColumn(
    val alias: String = "",
    val name: String = "",
    val parentAlias: String? = null,
    val coverPolicy: String = ContentImagePolicy.OPTIONAL.name,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val preset: Boolean = true,
)

data class SitePackageDefinition(
    val manifest: SitePackageManifest,
    val columns: List<SitePackageColumn>,
)

data class SiteProvisioningReport(
    val packageId: String,
    val version: String,
    val created: Int,
    val updated: Int,
    val unchanged: Int,
    val columns: Int,
)

class SitePackageValidationException(message: String) : RuntimeException(message)

@Service
class SitePackageLoader(private val objectMapper: ObjectMapper) {
    fun load(packageRoot: Path): SitePackageDefinition {
        val root = packageRoot.toAbsolutePath().normalize()
        val manifestFile = safeFile(root, "manifest.json")
        val manifest = objectMapper.readValue(manifestFile.toFile(), SitePackageManifest::class.java)
        validateManifest(manifest)

        val columnEntry = manifest.structure.single { it.type == COLUMNS_STRUCTURE_TYPE }
        val columnFile = safeFile(root, columnEntry.path)
        val actualDigest = sha256(columnFile)
        if (actualDigest != columnEntry.sha256) {
            throw SitePackageValidationException("Structure digest 不一致：${columnEntry.path}")
        }
        val columns = objectMapper.readValue(columnFile.toFile(), Array<SitePackageColumn>::class.java).toList()
        validateColumns(columns)
        return SitePackageDefinition(manifest, columns)
    }

    private fun validateManifest(manifest: SitePackageManifest) {
        if (!manifest.packageId.matches(PACKAGE_ID)) throw SitePackageValidationException("packageId 不合法：${manifest.packageId}")
        if (manifest.schemaVersion != SITE_PACKAGE_SCHEMA_VERSION) {
            throw SitePackageValidationException("不支持的 Site Package schemaVersion：${manifest.schemaVersion}")
        }
        if (manifest.version.isBlank() || manifest.version.length > 64) throw SitePackageValidationException("version 不能为空且不能超过 64 个字符")
        if (manifest.structure.isEmpty()) throw SitePackageValidationException("structure 不能为空")
        if (manifest.structure.map { it.type }.toSet().size != manifest.structure.size) {
            throw SitePackageValidationException("structure type 不能重复")
        }
        if (manifest.structure.map { it.path }.toSet().size != manifest.structure.size) {
            throw SitePackageValidationException("structure path 不能重复")
        }
        if (manifest.structure.count { it.type == COLUMNS_STRUCTURE_TYPE } != 1) {
            throw SitePackageValidationException("Site Package v1 必须且只能包含一个 columns structure")
        }
        val unsupported = manifest.structure.map { it.type }.filter { it != COLUMNS_STRUCTURE_TYPE }
        if (unsupported.isNotEmpty()) throw SitePackageValidationException("Site Package v1 暂不支持 structure type：${unsupported.joinToString()}")
        manifest.structure.forEach { entry ->
            if (entry.path.isBlank()) throw SitePackageValidationException("structure path 不能为空")
            if (!entry.sha256.matches(SHA256)) throw SitePackageValidationException("structure sha256 不合法：${entry.path}")
        }
    }

    private fun validateColumns(columns: List<SitePackageColumn>) {
        val aliases = mutableSetOf<String>()
        columns.forEach { column ->
            if (!column.alias.matches(COLUMN_ALIAS)) throw SitePackageValidationException("Column alias 不合法：${column.alias}")
            if (!aliases.add(column.alias)) throw SitePackageValidationException("Column alias 重复：${column.alias}")
            if (column.name.isBlank() || column.name.length > 100) throw SitePackageValidationException("Column name 不合法：${column.alias}")
            if (column.parentAlias != null && !column.parentAlias.matches(COLUMN_ALIAS)) {
                throw SitePackageValidationException("Column parentAlias 不合法：${column.alias}")
            }
            if (runCatching { ContentImagePolicy.valueOf(column.coverPolicy) }.isFailure) {
                throw SitePackageValidationException("Column coverPolicy 不合法：${column.alias}")
            }
            if (!column.preset) throw SitePackageValidationException("Site Package Column 必须声明 preset=true：${column.alias}")
        }
        orderedColumns(columns)
    }

    private fun safeFile(root: Path, relativePath: String): Path {
        val file = root.resolve(relativePath).normalize()
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw SitePackageValidationException("Site Package 文件不存在或路径越界：$relativePath")
        }
        return file
    }

    private fun sha256(file: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

@Service
class SitePackageProvisioner(
    private val loader: SitePackageLoader,
    private val dataSource: DataSource,
) {
    fun apply(packageRoot: Path): SiteProvisioningReport {
        val definition = loader.load(packageRoot)
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                preflightDatabase(connection, definition.columns)
                var created = 0
                var updated = 0
                var unchanged = 0
                orderedColumns(definition.columns).forEach { column ->
                    when (reconcileColumn(connection, column)) {
                        ReconcileResult.CREATED -> created++
                        ReconcileResult.UPDATED -> updated++
                        ReconcileResult.UNCHANGED -> unchanged++
                    }
                }
                connection.commit()
                return SiteProvisioningReport(
                    packageId = definition.manifest.packageId,
                    version = definition.manifest.version,
                    created = created,
                    updated = updated,
                    unchanged = unchanged,
                    columns = definition.columns.size,
                )
            } catch (error: Exception) {
                connection.rollback()
                throw error
            }
        }
    }

    private fun preflightDatabase(connection: Connection, columns: List<SitePackageColumn>) {
        val packageAliases = columns.mapTo(mutableSetOf()) { it.alias }
        columns.forEach { column ->
            val existing = findColumn(connection, column.alias)
            if (existing != null && !existing.preset) {
                throw SitePackageValidationException("Site Package 不接管 operator-created Column：${column.alias}")
            }
            val parentAlias = column.parentAlias
            if (parentAlias != null && parentAlias !in packageAliases) {
                val parent = findColumn(connection, parentAlias)
                    ?: throw SitePackageValidationException("Column parentAlias 不存在：${column.alias} -> $parentAlias")
                if (!parent.preset) throw SitePackageValidationException("Site Package Column 不能依赖 operator-created parent：$parentAlias")
            }
        }
    }

    private fun reconcileColumn(connection: Connection, column: SitePackageColumn): ReconcileResult {
        val parentId = column.parentAlias?.let { alias ->
            val parent = findColumn(connection, alias)
                ?: throw SitePackageValidationException("Column parentAlias 尚未 provision：${column.alias} -> $alias")
            if (!parent.preset) throw SitePackageValidationException("Site Package Column 不能依赖 operator-created parent：$alias")
            parent.id
        }
        val existing = findColumn(connection, column.alias)
        if (existing == null) {
            connection.prepareStatement(
                """
                INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
                VALUES (?, ?, ?, ?, ?, ?, 1)
                """.trimIndent(),
            ).use { statement ->
                statement.setObject(1, parentId)
                statement.setString(2, column.alias)
                statement.setString(3, column.name)
                statement.setString(4, column.coverPolicy)
                statement.setInt(5, column.sortOrder)
                statement.setBoolean(6, column.enabled)
                statement.executeUpdate()
            }
            return ReconcileResult.CREATED
        }
        if (!existing.preset) throw SitePackageValidationException("Site Package 不接管 operator-created Column：${column.alias}")
        if (
            existing.parentId == parentId &&
            existing.name == column.name &&
            existing.coverPolicy == column.coverPolicy &&
            existing.sortOrder == column.sortOrder &&
            existing.enabled == column.enabled
        ) {
            return ReconcileResult.UNCHANGED
        }
        connection.prepareStatement(
            """
            UPDATE cms_column
            SET parent_id=?, name=?, cover_policy=?, sort_order=?, enabled=?, preset=1
            WHERE id=?
            """.trimIndent(),
        ).use { statement ->
            statement.setObject(1, parentId)
            statement.setString(2, column.name)
            statement.setString(3, column.coverPolicy)
            statement.setInt(4, column.sortOrder)
            statement.setBoolean(5, column.enabled)
            statement.setLong(6, existing.id)
            statement.executeUpdate()
        }
        return ReconcileResult.UPDATED
    }

    private fun findColumn(connection: Connection, alias: String): ExistingColumn? =
        connection.prepareStatement(
            """
            SELECT id, parent_id, alias, name, cover_policy, sort_order, enabled, preset
            FROM cms_column
            WHERE alias=?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, alias)
            statement.executeQuery().use { result ->
                if (!result.next()) return@use null
                val parentId = result.getLong("parent_id").let { value -> if (result.wasNull()) null else value }
                ExistingColumn(
                    id = result.getLong("id"),
                    parentId = parentId,
                    alias = result.getString("alias"),
                    name = result.getString("name"),
                    coverPolicy = result.getString("cover_policy"),
                    sortOrder = result.getInt("sort_order"),
                    enabled = result.getBoolean("enabled"),
                    preset = result.getBoolean("preset"),
                )
            }
        }

    private data class ExistingColumn(
        val id: Long,
        val parentId: Long?,
        val alias: String,
        val name: String,
        val coverPolicy: String,
        val sortOrder: Int,
        val enabled: Boolean,
        val preset: Boolean,
    )

    private enum class ReconcileResult { CREATED, UPDATED, UNCHANGED }
}

private fun orderedColumns(columns: List<SitePackageColumn>): List<SitePackageColumn> {
    val byAlias = columns.associateBy { it.alias }
    val visiting = mutableSetOf<String>()
    val visited = mutableSetOf<String>()
    val result = mutableListOf<SitePackageColumn>()

    fun visit(alias: String) {
        if (alias in visited) return
        if (!visiting.add(alias)) throw SitePackageValidationException("Column parent relation 存在 cycle：$alias")
        val column = byAlias.getValue(alias)
        column.parentAlias?.takeIf { it in byAlias }?.let(::visit)
        visiting.remove(alias)
        visited.add(alias)
        result += column
    }

    columns.forEach { visit(it.alias) }
    return result
}

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "用法：provisionSitePackage <site-package-root> [Spring Boot args...]" }
    val packageRoot = Path.of(args.first()).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(*args.drop(1).toTypedArray())
    try {
        val report = context.getBean(SitePackageProvisioner::class.java).apply(packageRoot)
        println("SITE_PACKAGE_PROVISION_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
    } finally {
        context.close()
    }
}
