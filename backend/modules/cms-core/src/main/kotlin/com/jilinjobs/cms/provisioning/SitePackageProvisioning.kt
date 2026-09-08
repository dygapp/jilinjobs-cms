package com.jilinjobs.cms.provisioning

import com.jilinjobs.cms.common.ContentImagePolicy
import com.jilinjobs.cms.navigation.NavigationOpenMode
import com.jilinjobs.cms.navigation.NavigationTargetType
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

private val PACKAGE_ID = Regex("[a-z0-9][a-z0-9-]{0,63}")
private val LOWER_ALIAS = Regex("[a-z0-9][a-z0-9-]{0,99}")
private val STRUCTURE_CODE = Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,99}")
private val NAVIGATION_CODE = Regex("[a-z0-9][a-z0-9-]{0,99}")
private val CONFIG_KEY = Regex("[A-Z0-9][A-Z0-9_]{0,99}")
private val SHA256 = Regex("[0-9a-f]{64}")
private const val SITE_PACKAGE_SCHEMA_VERSION = 1
private const val COLUMNS = "columns"
private const val PAGE_GROUPS = "page-groups"
private const val PAGES = "pages"
private const val NAVIGATION_LOCATIONS = "navigation-locations"
private const val NAVIGATION_ITEMS = "navigation-items"
private const val SITE_CONFIG = "site-config"
private const val LISTS = "lists"
private const val ADVERTISEMENT_SLOTS = "advertisement-slots"
private val SUPPORTED_STRUCTURE_TYPES = setOf(
    COLUMNS,
    PAGE_GROUPS,
    PAGES,
    NAVIGATION_LOCATIONS,
    NAVIGATION_ITEMS,
    SITE_CONFIG,
    LISTS,
    ADVERTISEMENT_SLOTS,
)

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

data class SitePackagePageGroup(
    val alias: String = "",
    val name: String = "",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val preset: Boolean = true,
)

data class SitePackagePage(
    val groupAlias: String? = null,
    val alias: String = "",
    val name: String = "",
    val bodyHtml: String = "",
    val renderMode: String = "RICH_TEXT",
    val embedUrl: String? = null,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val preset: Boolean = true,
)

data class SitePackageNavigationLocation(
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val systemFlag: Boolean = false,
    val preset: Boolean = true,
)

data class SitePackageNavigationItem(
    val code: String = "",
    val parentCode: String? = null,
    val name: String = "",
    val locationCode: String = "",
    val category: String? = null,
    val targetType: String = NavigationTargetType.LINK.name,
    val targetColumnAlias: String? = null,
    val targetPageGroupAlias: String? = null,
    val targetPageAlias: String? = null,
    val targetUrl: String? = null,
    val openMode: String = NavigationOpenMode.DEFAULT.name,
    val iconPath: String? = null,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val preset: Boolean = true,
)

data class SitePackageConfig(
    val key: String = "",
    val propertyName: String = "",
    val groupCode: String = "GENERAL",
    val value: String = "",
    val valueType: String = "TEXT",
    val description: String = "",
    val sortOrder: Int = 0,
    val required: Boolean = false,
    val systemFlag: Boolean = false,
    val enabled: Boolean = true,
    val preset: Boolean = true,
)

data class SitePackageListDefinition(
    val code: String = "",
    val name: String = "",
    val groupCode: String = "GENERAL",
    val imagePolicy: String = ContentImagePolicy.OPTIONAL.name,
    val description: String = "",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val systemFlag: Boolean = false,
    val preset: Boolean = true,
)

data class SitePackageAdvertisementSlot(
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val systemFlag: Boolean = false,
    val preset: Boolean = true,
)

data class SitePackageDefinition(
    val manifest: SitePackageManifest,
    val columns: List<SitePackageColumn> = emptyList(),
    val pageGroups: List<SitePackagePageGroup> = emptyList(),
    val pages: List<SitePackagePage> = emptyList(),
    val navigationLocations: List<SitePackageNavigationLocation> = emptyList(),
    val navigationItems: List<SitePackageNavigationItem> = emptyList(),
    val siteConfig: List<SitePackageConfig> = emptyList(),
    val lists: List<SitePackageListDefinition> = emptyList(),
    val advertisementSlots: List<SitePackageAdvertisementSlot> = emptyList(),
) {
    val objectCount: Int
        get() = columns.size + pageGroups.size + pages.size + navigationLocations.size + navigationItems.size +
            siteConfig.size + lists.size + advertisementSlots.size
}

data class SiteProvisioningReport(
    val packageId: String,
    val version: String,
    val created: Int,
    val updated: Int,
    val unchanged: Int,
    val objects: Int,
    val columns: Int,
)

class SitePackageValidationException(message: String) : RuntimeException(message)

@Service
class SitePackageLoader(private val objectMapper: ObjectMapper) {
    fun load(packageRoot: Path): SitePackageDefinition {
        val root = packageRoot.toAbsolutePath().normalize()
        val manifest = objectMapper.readValue(safeFile(root, "manifest.json").toFile(), SitePackageManifest::class.java)
        validateManifest(manifest)
        val definition = SitePackageDefinition(
            manifest = manifest,
            columns = loadArray(root, manifest, COLUMNS, Array<SitePackageColumn>::class.java),
            pageGroups = loadArray(root, manifest, PAGE_GROUPS, Array<SitePackagePageGroup>::class.java),
            pages = loadArray(root, manifest, PAGES, Array<SitePackagePage>::class.java),
            navigationLocations = loadArray(root, manifest, NAVIGATION_LOCATIONS, Array<SitePackageNavigationLocation>::class.java),
            navigationItems = loadArray(root, manifest, NAVIGATION_ITEMS, Array<SitePackageNavigationItem>::class.java),
            siteConfig = loadArray(root, manifest, SITE_CONFIG, Array<SitePackageConfig>::class.java),
            lists = loadArray(root, manifest, LISTS, Array<SitePackageListDefinition>::class.java),
            advertisementSlots = loadArray(root, manifest, ADVERTISEMENT_SLOTS, Array<SitePackageAdvertisementSlot>::class.java),
        )
        validateDefinition(definition)
        return definition
    }

    private fun <T> loadArray(root: Path, manifest: SitePackageManifest, type: String, arrayType: Class<Array<T>>): List<T> {
        val entry = manifest.structure.singleOrNull { it.type == type } ?: return emptyList()
        val file = safeFile(root, entry.path)
        if (sha256(file) != entry.sha256) throw SitePackageValidationException("Structure digest 不一致：${entry.path}")
        return objectMapper.readValue(file.toFile(), arrayType).toList()
    }

    private fun validateManifest(manifest: SitePackageManifest) {
        if (!manifest.packageId.matches(PACKAGE_ID)) throw SitePackageValidationException("packageId 不合法：${manifest.packageId}")
        if (manifest.schemaVersion != SITE_PACKAGE_SCHEMA_VERSION) throw SitePackageValidationException("不支持的 Site Package schemaVersion：${manifest.schemaVersion}")
        if (manifest.version.isBlank() || manifest.version.length > 64) throw SitePackageValidationException("version 不合法")
        if (manifest.structure.isEmpty()) throw SitePackageValidationException("structure 不能为空")
        if (manifest.structure.map { it.type }.toSet().size != manifest.structure.size) throw SitePackageValidationException("structure type 不能重复")
        if (manifest.structure.map { it.path }.toSet().size != manifest.structure.size) throw SitePackageValidationException("structure path 不能重复")
        val unsupported = manifest.structure.map { it.type }.filter { it !in SUPPORTED_STRUCTURE_TYPES }
        if (unsupported.isNotEmpty()) throw SitePackageValidationException("Site Package v1 暂不支持 structure type：${unsupported.joinToString()}")
        manifest.structure.forEach {
            if (it.path.isBlank()) throw SitePackageValidationException("structure path 不能为空")
            if (!it.sha256.matches(SHA256)) throw SitePackageValidationException("structure sha256 不合法：${it.path}")
        }
    }

    private fun validateDefinition(definition: SitePackageDefinition) {
        unique(definition.columns.map { it.alias }, "Column alias")
        definition.columns.forEach {
            if (!it.alias.matches(LOWER_ALIAS)) throw SitePackageValidationException("Column alias 不合法：${it.alias}")
            name(it.name, "Column", it.alias)
            if (it.parentAlias != null && !it.parentAlias.matches(LOWER_ALIAS)) throw SitePackageValidationException("Column parentAlias 不合法：${it.alias}")
            if (runCatching { ContentImagePolicy.valueOf(it.coverPolicy) }.isFailure) throw SitePackageValidationException("Column coverPolicy 不合法：${it.alias}")
            preset(it.preset, "Column", it.alias)
        }
        orderedColumns(definition.columns)

        unique(definition.pageGroups.map { it.alias }, "PageGroup alias")
        definition.pageGroups.forEach {
            if (!it.alias.matches(LOWER_ALIAS)) throw SitePackageValidationException("PageGroup alias 不合法：${it.alias}")
            name(it.name, "PageGroup", it.alias)
            preset(it.preset, "PageGroup", it.alias)
        }

        unique(definition.pages.map { pageIdentity(it.groupAlias, it.alias) }, "Page logical identity")
        definition.pages.forEach {
            if (!it.alias.matches(LOWER_ALIAS)) throw SitePackageValidationException("Page alias 不合法：${it.alias}")
            if (it.groupAlias != null && !it.groupAlias.matches(LOWER_ALIAS)) throw SitePackageValidationException("Page groupAlias 不合法：${pageIdentity(it.groupAlias, it.alias)}")
            name(it.name, "Page", pageIdentity(it.groupAlias, it.alias))
            if (it.renderMode.isBlank() || it.renderMode.length > 32) throw SitePackageValidationException("Page renderMode 不合法：${it.alias}")
            if (it.embedUrl != null && it.embedUrl.length > 1000) throw SitePackageValidationException("Page embedUrl 过长：${it.alias}")
            preset(it.preset, "Page", pageIdentity(it.groupAlias, it.alias))
        }

        unique(definition.navigationLocations.map { it.code }, "NavigationLocation code")
        definition.navigationLocations.forEach {
            if (!it.code.matches(STRUCTURE_CODE) || it.code.length > 32) throw SitePackageValidationException("NavigationLocation code 不合法：${it.code}")
            name(it.name, "NavigationLocation", it.code)
            description(it.description, "NavigationLocation", it.code)
            preset(it.preset, "NavigationLocation", it.code)
        }

        unique(definition.navigationItems.map { it.code }, "NavigationItem code")
        definition.navigationItems.forEach {
            if (!it.code.matches(NAVIGATION_CODE)) throw SitePackageValidationException("NavigationItem code 不合法：${it.code}")
            if (it.parentCode != null && !it.parentCode.matches(NAVIGATION_CODE)) throw SitePackageValidationException("NavigationItem parentCode 不合法：${it.code}")
            name(it.name, "NavigationItem", it.code)
            if (!it.locationCode.matches(STRUCTURE_CODE) || it.locationCode.length > 32) throw SitePackageValidationException("NavigationItem locationCode 不合法：${it.code}")
            if (it.category != null && it.category.length > 50) throw SitePackageValidationException("NavigationItem category 过长：${it.code}")
            if (runCatching { NavigationTargetType.valueOf(it.targetType) }.isFailure) throw SitePackageValidationException("NavigationItem targetType 不合法：${it.code}")
            if (runCatching { NavigationOpenMode.valueOf(it.openMode) }.isFailure) throw SitePackageValidationException("NavigationItem openMode 不合法：${it.code}")
            if (it.targetColumnAlias != null && !it.targetColumnAlias.matches(LOWER_ALIAS)) throw SitePackageValidationException("NavigationItem targetColumnAlias 不合法：${it.code}")
            if (it.targetPageGroupAlias != null && !it.targetPageGroupAlias.matches(LOWER_ALIAS)) throw SitePackageValidationException("NavigationItem targetPageGroupAlias 不合法：${it.code}")
            if (it.targetPageAlias != null && !it.targetPageAlias.matches(LOWER_ALIAS)) throw SitePackageValidationException("NavigationItem targetPageAlias 不合法：${it.code}")
            if (it.targetUrl != null && it.targetUrl.length > 1000) throw SitePackageValidationException("NavigationItem targetUrl 过长：${it.code}")
            if (it.iconPath != null && it.iconPath.length > 500) throw SitePackageValidationException("NavigationItem iconPath 过长：${it.code}")
            validateNavigationTarget(it)
            preset(it.preset, "NavigationItem", it.code)
        }
        orderedNavigationItems(definition.navigationItems)

        unique(definition.siteConfig.map { it.key }, "SiteConfig key")
        definition.siteConfig.forEach {
            if (!it.key.matches(CONFIG_KEY)) throw SitePackageValidationException("SiteConfig key 不合法：${it.key}")
            name(it.propertyName, "SiteConfig", it.key)
            if (it.groupCode.isBlank() || it.groupCode.length > 50 || it.valueType.isBlank() || it.valueType.length > 32) throw SitePackageValidationException("SiteConfig metadata 不合法：${it.key}")
            description(it.description, "SiteConfig", it.key)
            preset(it.preset, "SiteConfig", it.key)
        }

        unique(definition.lists.map { it.code }, "CmsList code")
        definition.lists.forEach {
            if (!it.code.matches(STRUCTURE_CODE)) throw SitePackageValidationException("CmsList code 不合法：${it.code}")
            name(it.name, "CmsList", it.code)
            if (it.groupCode.isBlank() || it.groupCode.length > 50) throw SitePackageValidationException("CmsList groupCode 不合法：${it.code}")
            if (runCatching { ContentImagePolicy.valueOf(it.imagePolicy) }.isFailure) throw SitePackageValidationException("CmsList imagePolicy 不合法：${it.code}")
            description(it.description, "CmsList", it.code)
            preset(it.preset, "CmsList", it.code)
        }

        unique(definition.advertisementSlots.map { it.code }, "AdvertisementSlot code")
        definition.advertisementSlots.forEach {
            if (!it.code.matches(STRUCTURE_CODE)) throw SitePackageValidationException("AdvertisementSlot code 不合法：${it.code}")
            name(it.name, "AdvertisementSlot", it.code)
            description(it.description, "AdvertisementSlot", it.code)
            preset(it.preset, "AdvertisementSlot", it.code)
        }
    }

    private fun validateNavigationTarget(item: SitePackageNavigationItem) {
        val hasColumn = item.targetColumnAlias != null
        val hasPage = item.targetPageAlias != null
        val hasUrl = item.targetUrl != null
        when (NavigationTargetType.valueOf(item.targetType)) {
            NavigationTargetType.HOME, NavigationTargetType.PLACEHOLDER -> if (hasColumn || hasPage || hasUrl || item.targetPageGroupAlias != null) invalidTarget(item)
            NavigationTargetType.COLUMN -> if (!hasColumn || hasPage || hasUrl || item.targetPageGroupAlias != null) invalidTarget(item)
            NavigationTargetType.PAGE -> if (hasColumn || !hasPage || hasUrl) invalidTarget(item)
            NavigationTargetType.LINK -> if (hasColumn || hasPage || !hasUrl || item.targetPageGroupAlias != null) invalidTarget(item)
        }
    }

    private fun invalidTarget(item: SitePackageNavigationItem): Nothing =
        throw SitePackageValidationException("NavigationItem target contract 不合法：${item.code}")

    private fun unique(values: List<String>, label: String) {
        if (values.toSet().size != values.size) throw SitePackageValidationException("$label 重复")
    }

    private fun name(value: String, type: String, identity: String) {
        if (value.isBlank() || value.length > 100) throw SitePackageValidationException("$type name 不合法：$identity")
    }

    private fun description(value: String, type: String, identity: String) {
        if (value.length > 255) throw SitePackageValidationException("$type description 过长：$identity")
    }

    private fun preset(value: Boolean, type: String, identity: String) {
        if (!value) throw SitePackageValidationException("Site Package $type 必须声明 preset=true：$identity")
    }

    private fun safeFile(root: Path, relativePath: String): Path {
        val file = root.resolve(relativePath).normalize()
        if (!file.startsWith(root) || !Files.isRegularFile(file)) throw SitePackageValidationException("Site Package 文件不存在或路径越界：$relativePath")
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
class SitePackageProvisioner(private val loader: SitePackageLoader, private val dataSource: DataSource) {
    fun apply(packageRoot: Path): SiteProvisioningReport {
        val definition = loader.load(packageRoot)
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                preflight(connection, definition)
                val counts = Counts()
                orderedColumns(definition.columns).forEach { counts.add(reconcileColumn(connection, it)) }
                definition.pageGroups.forEach { counts.add(reconcilePageGroup(connection, it)) }
                definition.pages.forEach { counts.add(reconcilePage(connection, it)) }
                definition.navigationLocations.forEach { counts.add(reconcileNavigationLocation(connection, it)) }
                orderedNavigationItems(definition.navigationItems).forEach { counts.add(reconcileNavigationItem(connection, it)) }
                definition.siteConfig.forEach { counts.add(reconcileSiteConfig(connection, it)) }
                definition.lists.forEach { counts.add(reconcileList(connection, it)) }
                definition.advertisementSlots.forEach { counts.add(reconcileAdSlot(connection, it)) }
                connection.commit()
                return SiteProvisioningReport(
                    packageId = definition.manifest.packageId,
                    version = definition.manifest.version,
                    created = counts.created,
                    updated = counts.updated,
                    unchanged = counts.unchanged,
                    objects = definition.objectCount,
                    columns = definition.columns.size,
                )
            } catch (error: Exception) {
                connection.rollback()
                throw error
            }
        }
    }

    private fun preflight(connection: Connection, definition: SitePackageDefinition) {
        val packageColumns = definition.columns.mapTo(mutableSetOf()) { it.alias }
        definition.columns.forEach { column ->
            column(connection, column.alias)?.owned("Column", column.alias)
            column.parentAlias?.takeIf { it !in packageColumns }?.let { parentAlias ->
                val parent = column(connection, parentAlias) ?: throw SitePackageValidationException("Column parentAlias 不存在：${column.alias} -> $parentAlias")
                parent.owned("Column parent", parentAlias)
            }
        }
        val packageGroups = definition.pageGroups.mapTo(mutableSetOf()) { it.alias }
        definition.pageGroups.forEach { pageGroup(connection, it.alias)?.owned("PageGroup", it.alias) }
        definition.pages.forEach { page ->
            val identity = pageIdentity(page.groupAlias, page.alias)
            page.groupAlias?.takeIf { it !in packageGroups }?.let { groupAlias ->
                val group = pageGroup(connection, groupAlias) ?: throw SitePackageValidationException("Page groupAlias 不存在：$identity -> $groupAlias")
                group.owned("PageGroup", groupAlias)
            }
            page(connection, page.groupAlias, page.alias)?.owned("Page", identity)
        }
        definition.navigationLocations.forEach { navLocation(connection, it.code)?.owned("NavigationLocation", it.code) }
        definition.navigationItems.forEach { navigation(connection, it.code)?.owned("NavigationItem", it.code) }
        definition.siteConfig.forEach { siteConfig(connection, it.key)?.owned("SiteConfig", it.key) }
        definition.lists.forEach { list(connection, it.code)?.owned("CmsList", it.code) }
        definition.advertisementSlots.forEach { adSlot(connection, it.code)?.owned("AdvertisementSlot", it.code) }
    }

    private fun reconcileColumn(connection: Connection, target: SitePackageColumn): Result {
        val parentId = target.parentAlias?.let { alias ->
            val parent = column(connection, alias) ?: throw SitePackageValidationException("Column parentAlias 尚未 provision：${target.alias} -> $alias")
            parent.owned("Column parent", alias)
            parent.id
        }
        val existing = column(connection, target.alias)
        if (existing == null) {
            connection.prepareStatement("INSERT INTO cms_column(parent_id,alias,name,cover_policy,sort_order,enabled,preset) VALUES(?,?,?,?,?,?,1)").use {
                it.setObject(1, parentId); it.setString(2, target.alias); it.setString(3, target.name); it.setString(4, target.coverPolicy); it.setInt(5, target.sortOrder); it.setBoolean(6, target.enabled); it.executeUpdate()
            }
            return Result.CREATED
        }
        existing.owned("Column", target.alias)
        if (existing.parentId == parentId && existing.name == target.name && existing.coverPolicy == target.coverPolicy && existing.sortOrder == target.sortOrder && existing.enabled == target.enabled) return Result.UNCHANGED
        connection.prepareStatement("UPDATE cms_column SET parent_id=?,name=?,cover_policy=?,sort_order=?,enabled=?,preset=1 WHERE id=?").use {
            it.setObject(1, parentId); it.setString(2, target.name); it.setString(3, target.coverPolicy); it.setInt(4, target.sortOrder); it.setBoolean(5, target.enabled); it.setLong(6, existing.id); it.executeUpdate()
        }
        return Result.UPDATED
    }

    private fun reconcilePageGroup(connection: Connection, target: SitePackagePageGroup): Result {
        val existing = pageGroup(connection, target.alias)
        if (existing == null) {
            connection.prepareStatement("INSERT INTO cms_page_group(alias,name,sort_order,enabled,preset) VALUES(?,?,?,?,1)").use {
                it.setString(1, target.alias); it.setString(2, target.name); it.setInt(3, target.sortOrder); it.setBoolean(4, target.enabled); it.executeUpdate()
            }
            return Result.CREATED
        }
        existing.owned("PageGroup", target.alias)
        if (existing.name == target.name && existing.sortOrder == target.sortOrder && existing.enabled == target.enabled) return Result.UNCHANGED
        connection.prepareStatement("UPDATE cms_page_group SET name=?,sort_order=?,enabled=?,preset=1 WHERE id=?").use {
            it.setString(1, target.name); it.setInt(2, target.sortOrder); it.setBoolean(3, target.enabled); it.setLong(4, existing.id); it.executeUpdate()
        }
        return Result.UPDATED
    }

    private fun reconcilePage(connection: Connection, target: SitePackagePage): Result {
        val groupId = target.groupAlias?.let { alias ->
            val group = pageGroup(connection, alias) ?: throw SitePackageValidationException("Page groupAlias 尚未 provision：${pageIdentity(alias, target.alias)}")
            group.owned("PageGroup", alias)
            group.id
        }
        val identity = pageIdentity(target.groupAlias, target.alias)
        val existing = page(connection, target.groupAlias, target.alias)
        if (existing == null) {
            connection.prepareStatement("INSERT INTO cms_page(group_id,alias,name,body_html,render_mode,embed_url,sort_order,enabled,preset) VALUES(?,?,?,?,?,?,?,?,1)").use {
                it.setObject(1, groupId); it.setString(2, target.alias); it.setString(3, target.name); it.setString(4, target.bodyHtml); it.setString(5, target.renderMode); it.setString(6, target.embedUrl); it.setInt(7, target.sortOrder); it.setBoolean(8, target.enabled); it.executeUpdate()
            }
            return Result.CREATED
        }
        existing.owned("Page", identity)
        if (existing.groupId == groupId && existing.name == target.name && existing.sortOrder == target.sortOrder && existing.enabled == target.enabled) return Result.UNCHANGED
        connection.prepareStatement("UPDATE cms_page SET group_id=?,name=?,sort_order=?,enabled=?,preset=1 WHERE id=?").use {
            it.setObject(1, groupId); it.setString(2, target.name); it.setInt(3, target.sortOrder); it.setBoolean(4, target.enabled); it.setLong(5, existing.id); it.executeUpdate()
        }
        return Result.UPDATED
    }

    private fun reconcileNavigationLocation(connection: Connection, target: SitePackageNavigationLocation): Result =
        reconcileNamed(connection, "NavigationLocation", target.code, navLocation(connection, target.code), target.name, target.description, target.sortOrder, target.enabled, target.systemFlag, "cms_navigation_location", "code")

    private fun reconcileNavigationItem(connection: Connection, target: SitePackageNavigationItem): Result {
        val parentId = target.parentCode?.let { code ->
            val parent = navigation(connection, code) ?: throw SitePackageValidationException("NavigationItem parentCode 尚未 provision：${target.code} -> $code")
            parent.owned("NavigationItem parent", code)
            parent.id
        }
        val columnId = target.targetColumnAlias?.let { alias ->
            val column = column(connection, alias) ?: throw SitePackageValidationException("NavigationItem targetColumnAlias 不存在：${target.code} -> $alias")
            column.owned("Column target", alias)
            column.id
        }
        val pageId = target.targetPageAlias?.let { alias ->
            val page = page(connection, target.targetPageGroupAlias, alias)
                ?: throw SitePackageValidationException("NavigationItem targetPage 不存在：${target.code} -> ${pageIdentity(target.targetPageGroupAlias, alias)}")
            page.owned("Page target", pageIdentity(target.targetPageGroupAlias, alias))
            page.id
        }
        val location = navLocation(connection, target.locationCode)
            ?: throw SitePackageValidationException("NavigationItem locationCode 不存在：${target.code} -> ${target.locationCode}")
        location.owned("NavigationLocation target", target.locationCode)

        val existing = navigation(connection, target.code)
        if (existing != null) {
            existing.owned("NavigationItem", target.code)
            if (existing.matches(target, parentId, columnId, pageId)) return Result.UNCHANGED
            updateNavigation(connection, existing.id, target, parentId, columnId, pageId, keepCode = target.code)
            return Result.UPDATED
        }

        val legacy = legacyNavigations(connection)
        if (legacy.isNotEmpty()) {
            val matches = legacy.filter { it.matches(target, parentId, columnId, pageId) }
            if (matches.size != 1) {
                throw SitePackageValidationException("Legacy NavigationItem 无法无歧义认领 stable code：${target.code}")
            }
            updateNavigation(connection, matches.single().id, target, parentId, columnId, pageId, keepCode = target.code)
            return Result.UPDATED
        }

        connection.prepareStatement(
            "INSERT INTO cms_navigation(code,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,icon_path,sort_order,enabled,preset) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,1)",
        ).use {
            it.setString(1, target.code); it.setObject(2, parentId); it.setString(3, target.name); it.setString(4, target.locationCode); it.setString(5, target.category)
            it.setString(6, target.targetType); it.setObject(7, columnId); it.setObject(8, pageId); it.setString(9, target.targetUrl); it.setString(10, target.openMode)
            it.setString(11, target.iconPath); it.setInt(12, target.sortOrder); it.setBoolean(13, target.enabled); it.executeUpdate()
        }
        return Result.CREATED
    }

    private fun updateNavigation(
        connection: Connection,
        id: Long,
        target: SitePackageNavigationItem,
        parentId: Long?,
        columnId: Long?,
        pageId: Long?,
        keepCode: String,
    ) {
        connection.prepareStatement(
            "UPDATE cms_navigation SET code=?,parent_id=?,name=?,position=?,category=?,target_type=?,target_column_id=?,target_page_id=?,target_url=?,open_mode=?,icon_path=?,sort_order=?,enabled=?,preset=1 WHERE id=?",
        ).use {
            it.setString(1, keepCode); it.setObject(2, parentId); it.setString(3, target.name); it.setString(4, target.locationCode); it.setString(5, target.category)
            it.setString(6, target.targetType); it.setObject(7, columnId); it.setObject(8, pageId); it.setString(9, target.targetUrl); it.setString(10, target.openMode)
            it.setString(11, target.iconPath); it.setInt(12, target.sortOrder); it.setBoolean(13, target.enabled); it.setLong(14, id); it.executeUpdate()
        }
    }

    private fun reconcileAdSlot(connection: Connection, target: SitePackageAdvertisementSlot): Result =
        reconcileNamed(connection, "AdvertisementSlot", target.code, adSlot(connection, target.code), target.name, target.description, target.sortOrder, target.enabled, target.systemFlag, "cms_ad_slot", "code")

    private fun reconcileNamed(
        connection: Connection,
        type: String,
        key: String,
        existing: ExistingNamed?,
        name: String,
        description: String,
        sortOrder: Int,
        enabled: Boolean,
        systemFlag: Boolean,
        table: String,
        keyColumn: String,
    ): Result {
        if (existing == null) {
            connection.prepareStatement("INSERT INTO $table($keyColumn,name,description,sort_order,enabled,system_flag,preset) VALUES(?,?,?,?,?,?,1)").use {
                it.setString(1, key); it.setString(2, name); it.setString(3, description); it.setInt(4, sortOrder); it.setBoolean(5, enabled); it.setBoolean(6, systemFlag); it.executeUpdate()
            }
            return Result.CREATED
        }
        existing.owned(type, key)
        if (existing.name == name && existing.description == description && existing.sortOrder == sortOrder && existing.enabled == enabled && existing.systemFlag == systemFlag) return Result.UNCHANGED
        connection.prepareStatement("UPDATE $table SET name=?,description=?,sort_order=?,enabled=?,system_flag=?,preset=1 WHERE $keyColumn=?").use {
            it.setString(1, name); it.setString(2, description); it.setInt(3, sortOrder); it.setBoolean(4, enabled); it.setBoolean(5, systemFlag); it.setString(6, key); it.executeUpdate()
        }
        return Result.UPDATED
    }

    private fun reconcileSiteConfig(connection: Connection, target: SitePackageConfig): Result {
        val existing = siteConfig(connection, target.key)
        if (existing == null) {
            connection.prepareStatement("INSERT INTO cms_site_config(config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system_flag,enabled,preset) VALUES(?,?,?,?,?,?,?,?,?,?,1)").use {
                it.setString(1, target.key); it.setString(2, target.propertyName); it.setString(3, target.groupCode); it.setString(4, target.value); it.setString(5, target.valueType); it.setString(6, target.description); it.setInt(7, target.sortOrder); it.setBoolean(8, target.required); it.setBoolean(9, target.systemFlag); it.setBoolean(10, target.enabled); it.executeUpdate()
            }
            return Result.CREATED
        }
        existing.owned("SiteConfig", target.key)
        if (existing.propertyName == target.propertyName && existing.groupCode == target.groupCode && existing.value == target.value && existing.valueType == target.valueType && existing.description == target.description && existing.sortOrder == target.sortOrder && existing.required == target.required && existing.systemFlag == target.systemFlag && existing.enabled == target.enabled) return Result.UNCHANGED
        connection.prepareStatement("UPDATE cms_site_config SET property_name=?,group_code=?,config_value=?,value_type=?,description=?,sort_order=?,required=?,system_flag=?,enabled=?,preset=1 WHERE config_key=?").use {
            it.setString(1, target.propertyName); it.setString(2, target.groupCode); it.setString(3, target.value); it.setString(4, target.valueType); it.setString(5, target.description); it.setInt(6, target.sortOrder); it.setBoolean(7, target.required); it.setBoolean(8, target.systemFlag); it.setBoolean(9, target.enabled); it.setString(10, target.key); it.executeUpdate()
        }
        return Result.UPDATED
    }

    private fun reconcileList(connection: Connection, target: SitePackageListDefinition): Result {
        val existing = list(connection, target.code)
        if (existing == null) {
            connection.prepareStatement("INSERT INTO cms_list(code,name,group_code,image_policy,description,sort_order,enabled,system_flag,preset) VALUES(?,?,?,?,?,?,?,?,1)").use {
                it.setString(1, target.code); it.setString(2, target.name); it.setString(3, target.groupCode); it.setString(4, target.imagePolicy); it.setString(5, target.description); it.setInt(6, target.sortOrder); it.setBoolean(7, target.enabled); it.setBoolean(8, target.systemFlag); it.executeUpdate()
            }
            return Result.CREATED
        }
        existing.owned("CmsList", target.code)
        if (existing.name == target.name && existing.groupCode == target.groupCode && existing.imagePolicy == target.imagePolicy && existing.description == target.description && existing.sortOrder == target.sortOrder && existing.enabled == target.enabled && existing.systemFlag == target.systemFlag) return Result.UNCHANGED
        connection.prepareStatement("UPDATE cms_list SET name=?,group_code=?,image_policy=?,description=?,sort_order=?,enabled=?,system_flag=?,preset=1 WHERE code=?").use {
            it.setString(1, target.name); it.setString(2, target.groupCode); it.setString(3, target.imagePolicy); it.setString(4, target.description); it.setInt(5, target.sortOrder); it.setBoolean(6, target.enabled); it.setBoolean(7, target.systemFlag); it.setString(8, target.code); it.executeUpdate()
        }
        return Result.UPDATED
    }

    private fun column(connection: Connection, alias: String): ExistingColumn? = connection.prepareStatement("SELECT id,parent_id,name,cover_policy,sort_order,enabled,preset FROM cms_column WHERE alias=?").use { statement ->
        statement.setString(1, alias)
        statement.executeQuery().use { result -> if (!result.next()) null else ExistingColumn(result.getLong("id"), result.nullableLong("parent_id"), result.getString("name"), result.getString("cover_policy"), result.getInt("sort_order"), result.getBoolean("enabled"), result.getBoolean("preset")) }
    }

    private fun pageGroup(connection: Connection, alias: String): ExistingPageGroup? = connection.prepareStatement("SELECT id,name,sort_order,enabled,preset FROM cms_page_group WHERE alias=?").use { statement ->
        statement.setString(1, alias)
        statement.executeQuery().use { result -> if (!result.next()) null else ExistingPageGroup(result.getLong("id"), result.getString("name"), result.getInt("sort_order"), result.getBoolean("enabled"), result.getBoolean("preset")) }
    }

    private fun page(connection: Connection, groupAlias: String?, alias: String): ExistingPage? {
        val sql = if (groupAlias == null) {
            "SELECT id,group_id,name,body_html,render_mode,embed_url,sort_order,enabled,preset FROM cms_page WHERE group_id IS NULL AND alias=?"
        } else {
            "SELECT p.id,p.group_id,p.name,p.body_html,p.render_mode,p.embed_url,p.sort_order,p.enabled,p.preset FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias=? AND p.alias=?"
        }
        return connection.prepareStatement(sql).use { statement ->
            if (groupAlias == null) statement.setString(1, alias) else { statement.setString(1, groupAlias); statement.setString(2, alias) }
            statement.executeQuery().use { result ->
                if (!result.next()) return@use null
                val row = ExistingPage(result.getLong("id"), result.nullableLong("group_id"), result.getString("name"), result.getString("body_html"), result.getString("render_mode"), result.getString("embed_url"), result.getInt("sort_order"), result.getBoolean("enabled"), result.getBoolean("preset"))
                if (result.next()) throw SitePackageValidationException("Page logical identity 存在歧义：${pageIdentity(groupAlias, alias)}")
                row
            }
        }
    }

    private fun navigation(connection: Connection, code: String): ExistingNavigation? = connection.prepareStatement(
        "SELECT id,code,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,icon_path,sort_order,enabled,preset FROM cms_navigation WHERE code=?",
    ).use { statement ->
        statement.setString(1, code)
        statement.executeQuery().use { result -> if (!result.next()) null else result.navigation() }
    }

    private fun legacyNavigations(connection: Connection): List<ExistingNavigation> = connection.prepareStatement(
        "SELECT id,code,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,icon_path,sort_order,enabled,preset FROM cms_navigation WHERE code IS NULL AND preset=1 ORDER BY id",
    ).use { statement ->
        statement.executeQuery().use { result -> buildList { while (result.next()) add(result.navigation()) } }
    }

    private fun navLocation(connection: Connection, code: String): ExistingNamed? = named(connection, "cms_navigation_location", "code", code)
    private fun adSlot(connection: Connection, code: String): ExistingNamed? = named(connection, "cms_ad_slot", "code", code)

    private fun named(connection: Connection, table: String, keyColumn: String, key: String): ExistingNamed? = connection.prepareStatement("SELECT name,description,sort_order,enabled,system_flag,preset FROM $table WHERE $keyColumn=?").use { statement ->
        statement.setString(1, key)
        statement.executeQuery().use { result -> if (!result.next()) null else ExistingNamed(result.getString("name"), result.getString("description"), result.getInt("sort_order"), result.getBoolean("enabled"), result.getBoolean("system_flag"), result.getBoolean("preset")) }
    }

    private fun siteConfig(connection: Connection, key: String): ExistingConfig? = connection.prepareStatement("SELECT property_name,group_code,config_value,value_type,description,sort_order,required,system_flag,enabled,preset FROM cms_site_config WHERE config_key=?").use { statement ->
        statement.setString(1, key)
        statement.executeQuery().use { result -> if (!result.next()) null else ExistingConfig(result.getString("property_name"), result.getString("group_code"), result.getString("config_value"), result.getString("value_type"), result.getString("description"), result.getInt("sort_order"), result.getBoolean("required"), result.getBoolean("system_flag"), result.getBoolean("enabled"), result.getBoolean("preset")) }
    }

    private fun list(connection: Connection, code: String): ExistingList? = connection.prepareStatement("SELECT name,group_code,image_policy,description,sort_order,enabled,system_flag,preset FROM cms_list WHERE code=?").use { statement ->
        statement.setString(1, code)
        statement.executeQuery().use { result -> if (!result.next()) null else ExistingList(result.getString("name"), result.getString("group_code"), result.getString("image_policy"), result.getString("description"), result.getInt("sort_order"), result.getBoolean("enabled"), result.getBoolean("system_flag"), result.getBoolean("preset")) }
    }

    private interface Owned { val preset: Boolean }
    private fun Owned.owned(type: String, identity: String) {
        if (!preset) throw SitePackageValidationException("Site Package 不接管 operator-created $type：$identity")
    }

    private data class ExistingColumn(val id: Long, val parentId: Long?, val name: String, val coverPolicy: String, val sortOrder: Int, val enabled: Boolean, override val preset: Boolean) : Owned
    private data class ExistingPageGroup(val id: Long, val name: String, val sortOrder: Int, val enabled: Boolean, override val preset: Boolean) : Owned
    private data class ExistingPage(val id: Long, val groupId: Long?, val name: String, val bodyHtml: String, val renderMode: String, val embedUrl: String?, val sortOrder: Int, val enabled: Boolean, override val preset: Boolean) : Owned
    private data class ExistingNamed(val name: String, val description: String, val sortOrder: Int, val enabled: Boolean, val systemFlag: Boolean, override val preset: Boolean) : Owned
    private data class ExistingConfig(val propertyName: String, val groupCode: String, val value: String, val valueType: String, val description: String, val sortOrder: Int, val required: Boolean, val systemFlag: Boolean, val enabled: Boolean, override val preset: Boolean) : Owned
    private data class ExistingList(val name: String, val groupCode: String, val imagePolicy: String, val description: String, val sortOrder: Int, val enabled: Boolean, val systemFlag: Boolean, override val preset: Boolean) : Owned
    private data class ExistingNavigation(
        val id: Long,
        val code: String?,
        val parentId: Long?,
        val name: String,
        val position: String,
        val category: String?,
        val targetType: String,
        val targetColumnId: Long?,
        val targetPageId: Long?,
        val targetUrl: String?,
        val openMode: String,
        val iconPath: String?,
        val sortOrder: Int,
        val enabled: Boolean,
        override val preset: Boolean,
    ) : Owned {
        fun matches(target: SitePackageNavigationItem, expectedParentId: Long?, expectedColumnId: Long?, expectedPageId: Long?): Boolean =
            parentId == expectedParentId && name == target.name && position == target.locationCode && category == target.category &&
                targetType == target.targetType && targetColumnId == expectedColumnId && targetPageId == expectedPageId && targetUrl == target.targetUrl &&
                openMode == target.openMode && iconPath == target.iconPath && sortOrder == target.sortOrder && enabled == target.enabled
    }

    private fun ResultSet.navigation() = ExistingNavigation(
        getLong("id"), getString("code"), nullableLong("parent_id"), getString("name"), getString("position"), getString("category"),
        getString("target_type"), nullableLong("target_column_id"), nullableLong("target_page_id"), getString("target_url"), getString("open_mode"), getString("icon_path"),
        getInt("sort_order"), getBoolean("enabled"), getBoolean("preset"),
    )

    private enum class Result { CREATED, UPDATED, UNCHANGED }
    private data class Counts(var created: Int = 0, var updated: Int = 0, var unchanged: Int = 0) {
        fun add(result: Result) = when (result) { Result.CREATED -> created++; Result.UPDATED -> updated++; Result.UNCHANGED -> unchanged++ }
    }
}

private fun ResultSet.nullableLong(column: String): Long? = getLong(column).let { if (wasNull()) null else it }
private fun pageIdentity(groupAlias: String?, alias: String): String = "${groupAlias ?: "<root>"}:$alias"

private fun orderedColumns(columns: List<SitePackageColumn>): List<SitePackageColumn> {
    val byAlias = columns.associateBy { it.alias }
    val visiting = mutableSetOf<String>()
    val visited = mutableSetOf<String>()
    val ordered = mutableListOf<SitePackageColumn>()
    fun visit(alias: String) {
        if (alias in visited) return
        if (!visiting.add(alias)) throw SitePackageValidationException("Column parent relation 存在 cycle：$alias")
        val column = byAlias.getValue(alias)
        column.parentAlias?.takeIf { it in byAlias }?.let(::visit)
        visiting.remove(alias)
        visited.add(alias)
        ordered += column
    }
    columns.forEach { visit(it.alias) }
    return ordered
}

private fun orderedNavigationItems(items: List<SitePackageNavigationItem>): List<SitePackageNavigationItem> {
    val byCode = items.associateBy { it.code }
    val visiting = mutableSetOf<String>()
    val visited = mutableSetOf<String>()
    val ordered = mutableListOf<SitePackageNavigationItem>()
    fun visit(code: String) {
        if (code in visited) return
        if (!visiting.add(code)) throw SitePackageValidationException("NavigationItem parent relation 存在 cycle：$code")
        val item = byCode.getValue(code)
        item.parentCode?.takeIf { it in byCode }?.let(::visit)
        visiting.remove(code)
        visited.add(code)
        ordered += item
    }
    items.forEach { visit(it.code) }
    return ordered
}
