package com.jilinjobs.cms.sitepackage

import com.jilinjobs.cms.CmsApplication
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

private val PACKAGE_ID_PATTERN = Regex("[a-z0-9][a-z0-9-]{0,63}")
private val COLUMN_ALIAS_PATTERN = Regex("[a-z0-9][a-z0-9-]{0,99}")
private val SHA256_PATTERN = Regex("[0-9a-f]{64}")
private val COVER_POLICIES = setOf("NONE", "OPTIONAL", "REQUIRED")

data class SitePackageManifest(
    val packageId: String,
    val schemaVersion: Int,
    val version: String,
    val structure: List<SitePackageStructureFile>,
)

data class SitePackageStructureFile(
    val kind: String,
    val path: String,
    val sha256: String,
)

data class SitePackageColumnsDocument(
    val columns: List<SitePackageColumnDefinition>,
)

data class SitePackageColumnDefinition(
    val alias: String,
    val name: String,
    val parentAlias: String? = null,
    val coverPolicy: String = "OPTIONAL",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
)

data class LoadedSitePackage(
    val manifest: SitePackageManifest,
    val columns: List<SitePackageColumnDefinition>,
)

enum class SitePackageProvisionStatus { INSERTED, UPDATED, UNCHANGED, CONFLICT }

data class SitePackageProvisionResult(
    val identity: String,
    val status: SitePackageProvisionStatus,
    val message: String? = null,
)

data class SitePackageProvisionReport(
    val packageId: String,
    val version: String,
    val total: Int,
    val inserted: Int,
    val updated: Int,
    val unchanged: Int,
    val conflicts: Int,
    val results: List<SitePackageProvisionResult>,
)

@Service
class SitePackageLoader(private val objectMapper: ObjectMapper) {
    fun load(packageRoot: Path): LoadedSitePackage {
        val root = packageRoot.toAbsolutePath().normalize()
        val manifestPath = root.resolve("manifest.json")
        require(Files.isRegularFile(manifestPath)) { "Site Package 缺少 manifest.json：$root" }

        val manifest = Files.newBufferedReader(manifestPath).use {
            objectMapper.readValue(it, SitePackageManifest::class.java)
        }
        validateManifest(manifest)

        val structureFiles = manifest.structure.associateBy { it.kind }
        require(structureFiles.size == manifest.structure.size) { "Site Package structure kind 不能重复" }
        require(structureFiles.keys == setOf("columns")) {
            "Site Package schemaVersion=1 Foundation 当前只接受 columns structure"
        }

        val columnsEntry = requireNotNull(structureFiles["columns"])
        val columnsPath = resolveAndVerify(root, columnsEntry)
        val columnsDocument = Files.newBufferedReader(columnsPath).use {
            objectMapper.readValue(it, SitePackageColumnsDocument::class.java)
        }
        validateColumns(columnsDocument.columns)

        return LoadedSitePackage(manifest, columnsDocument.columns)
    }

    private fun validateManifest(manifest: SitePackageManifest) {
        require(manifest.packageId.matches(PACKAGE_ID_PATTERN)) { "packageId 不合法：${manifest.packageId}" }
        require(manifest.schemaVersion == 1) { "不支持的 Site Package schemaVersion：${manifest.schemaVersion}" }
        require(manifest.version.isNotBlank() && manifest.version.length <= 64) { "Site Package version 不合法" }
        require(manifest.structure.isNotEmpty()) { "Site Package structure 不能为空" }
        manifest.structure.forEach {
            require(it.kind.isNotBlank()) { "structure kind 不能为空" }
            require(it.path.isNotBlank()) { "structure path 不能为空" }
            require(it.sha256.matches(SHA256_PATTERN)) { "structure SHA-256 不合法：${it.path}" }
        }
    }

    private fun resolveAndVerify(root: Path, entry: SitePackageStructureFile): Path {
        val relative = Path.of(entry.path)
        require(!relative.isAbsolute) { "Site Package structure path 必须是相对路径：${entry.path}" }
        val resolved = root.resolve(relative).normalize()
        require(resolved.startsWith(root) && resolved != root && Files.isRegularFile(resolved)) {
            "Site Package structure 文件不存在或路径越界：${entry.path}"
        }
        val digest = sha256(resolved)
        require(digest == entry.sha256) { "Site Package structure digest 不匹配：${entry.path}" }
        return resolved
    }

    internal fun validateColumns(columns: List<SitePackageColumnDefinition>) {
        require(columns.isNotEmpty()) { "columns structure 不能为空" }
        val byAlias = linkedMapOf<String, SitePackageColumnDefinition>()
        columns.forEach { column ->
            require(column.alias.matches(COLUMN_ALIAS_PATTERN)) { "Column alias 不合法：${column.alias}" }
            require(column.name.isNotBlank() && column.name.length <= 100) { "Column name 不合法：${column.alias}" }
            require(column.parentAlias?.matches(COLUMN_ALIAS_PATTERN) != false) { "Column parentAlias 不合法：${column.alias}" }
            require(column.parentAlias != column.alias) { "Column 不能以自身作为 parent：${column.alias}" }
            require(column.coverPolicy in COVER_POLICIES) { "Column coverPolicy 不合法：${column.alias}" }
            require(byAlias.put(column.alias, column) == null) { "Column alias 重复：${column.alias}" }
        }

        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        fun visit(alias: String) {
            if (alias in visited) return
            require(visiting.add(alias)) { "Column parent relationship 存在循环：$alias" }
            val parent = byAlias[alias]?.parentAlias
            if (parent != null && parent in byAlias) visit(parent)
            visiting.remove(alias)
            visited.add(alias)
        }
        byAlias.keys.forEach(::visit)
    }

    private fun sha256(path: Path): String {
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
}

@Mapper
interface SitePackageColumnMapper {
    @Select(
        """
        SELECT id, parent_id, alias, name, cover_policy, sort_order, enabled, preset
        FROM cms_column
        WHERE alias=#{alias}
        """,
    )
    fun findByAlias(@Param("alias") alias: String): SitePackageColumnRecord?

    @Insert(
        """
        INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
        VALUES(#{parentId}, #{alias}, #{name}, #{coverPolicy}, #{sortOrder}, #{enabled}, 1)
        """,
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(record: SitePackageColumnRecord): Int

    @Update(
        """
        UPDATE cms_column
        SET parent_id=#{parentId}, name=#{name}, cover_policy=#{coverPolicy},
            sort_order=#{sortOrder}, enabled=#{enabled}, preset=1
        WHERE id=#{id} AND alias=#{alias} AND preset=1
        """,
    )
    fun updatePreset(record: SitePackageColumnRecord): Int
}

data class SitePackageColumnRecord(
    var id: Long? = null,
    var parentId: Long? = null,
    var alias: String = "",
    var name: String = "",
    var coverPolicy: String = "OPTIONAL",
    var sortOrder: Int = 0,
    var enabled: Boolean = true,
    var preset: Boolean = false,
)

@Service
class SitePackageProvisioner(
    private val loader: SitePackageLoader,
    private val columnMapper: SitePackageColumnMapper,
) {
    @Transactional
    fun apply(packageRoot: Path): SitePackageProvisionReport = applyLoaded(loader.load(packageRoot))

    internal fun applyLoaded(sitePackage: LoadedSitePackage): SitePackageProvisionReport {
        val definitions = sitePackage.columns
        val packageAliases = definitions.mapTo(linkedSetOf()) { it.alias }
        val conflictResults = mutableListOf<SitePackageProvisionResult>()

        definitions.forEach { definition ->
            val existing = columnMapper.findByAlias(definition.alias)
            if (existing != null && !existing.preset) {
                conflictResults += SitePackageProvisionResult(
                    definition.alias,
                    SitePackageProvisionStatus.CONFLICT,
                    "stable alias 已被 operator-created preset=false Column 占用",
                )
            }
            val parentAlias = definition.parentAlias
            if (parentAlias != null && parentAlias !in packageAliases) {
                val parent = columnMapper.findByAlias(parentAlias)
                if (parent == null) {
                    conflictResults += SitePackageProvisionResult(
                        definition.alias,
                        SitePackageProvisionStatus.CONFLICT,
                        "外部 parent alias 不存在：$parentAlias",
                    )
                } else if (!parent.preset) {
                    conflictResults += SitePackageProvisionResult(
                        definition.alias,
                        SitePackageProvisionStatus.CONFLICT,
                        "外部 parent alias 不是受控 preset：$parentAlias",
                    )
                }
            }
        }

        if (conflictResults.isNotEmpty()) {
            return report(sitePackage, definitions.size, conflictResults)
        }

        val results = mutableListOf<SitePackageProvisionResult>()
        ordered(definitions).forEach { definition ->
            val parentId = definition.parentAlias?.let { parentAlias ->
                val parent = columnMapper.findByAlias(parentAlias)
                    ?: error("preflight 后 parent alias 丢失：$parentAlias")
                require(parent.preset) { "Site Package parent 必须是 preset：$parentAlias" }
                requireNotNull(parent.id)
            }
            val existing = columnMapper.findByAlias(definition.alias)
            if (existing == null) {
                val record = definition.toRecord(parentId)
                check(columnMapper.insert(record) == 1) { "创建 Site Package Column 失败：${definition.alias}" }
                results += SitePackageProvisionResult(definition.alias, SitePackageProvisionStatus.INSERTED)
            } else {
                check(existing.preset) { "preflight 后 stable alias 被非 preset 对象占用：${definition.alias}" }
                if (existing.matches(definition, parentId)) {
                    results += SitePackageProvisionResult(definition.alias, SitePackageProvisionStatus.UNCHANGED)
                } else {
                    val updated = definition.toRecord(parentId).copy(id = existing.id, preset = true)
                    check(columnMapper.updatePreset(updated) == 1) { "更新 Site Package Column 失败：${definition.alias}" }
                    results += SitePackageProvisionResult(definition.alias, SitePackageProvisionStatus.UPDATED)
                }
            }
        }
        return report(sitePackage, definitions.size, results)
    }

    private fun ordered(definitions: List<SitePackageColumnDefinition>): List<SitePackageColumnDefinition> {
        val packageAliases = definitions.mapTo(linkedSetOf()) { it.alias }
        val resolved = mutableSetOf<String>()
        val remaining = definitions.toMutableList()
        val ordered = mutableListOf<SitePackageColumnDefinition>()
        while (remaining.isNotEmpty()) {
            val ready = remaining.filter {
                it.parentAlias == null || it.parentAlias !in packageAliases || it.parentAlias in resolved
            }
            check(ready.isNotEmpty()) { "Column parent relationship 无法解析" }
            ready.forEach {
                ordered += it
                resolved += it.alias
            }
            remaining.removeAll(ready.toSet())
        }
        return ordered
    }

    private fun SitePackageColumnDefinition.toRecord(parentId: Long?) = SitePackageColumnRecord(
        parentId = parentId,
        alias = alias,
        name = name,
        coverPolicy = coverPolicy,
        sortOrder = sortOrder,
        enabled = enabled,
        preset = true,
    )

    private fun SitePackageColumnRecord.matches(
        definition: SitePackageColumnDefinition,
        expectedParentId: Long?,
    ): Boolean =
        preset &&
            parentId == expectedParentId &&
            name == definition.name &&
            coverPolicy == definition.coverPolicy &&
            sortOrder == definition.sortOrder &&
            enabled == definition.enabled

    private fun report(
        sitePackage: LoadedSitePackage,
        total: Int,
        results: List<SitePackageProvisionResult>,
    ) = SitePackageProvisionReport(
        packageId = sitePackage.manifest.packageId,
        version = sitePackage.manifest.version,
        total = total,
        inserted = results.count { it.status == SitePackageProvisionStatus.INSERTED },
        updated = results.count { it.status == SitePackageProvisionStatus.UPDATED },
        unchanged = results.count { it.status == SitePackageProvisionStatus.UNCHANGED },
        conflicts = results.count { it.status == SitePackageProvisionStatus.CONFLICT },
        results = results,
    )
}

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "用法：provisionSitePackage <package-root> [Spring Boot args...]" }
    val packageRoot = Path.of(args.first()).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(*args.drop(1).toTypedArray())
    try {
        val report = context.getBean(SitePackageProvisioner::class.java).apply(packageRoot)
        println("SITE_PACKAGE_PROVISION_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
        require(report.conflicts == 0) { "Site Package provision 存在 conflict，拒绝静默完成" }
    } finally {
        context.close()
    }
}
