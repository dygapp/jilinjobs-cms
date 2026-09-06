package com.jilinjobs.cms.sitepackage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

class SitePackageProvisionerTest {
    @Test
    fun `provisions parent-child columns idempotently and keeps unrelated operator data`() {
        val mapper = FakeSitePackageColumnMapper()
        mapper.seed(
            SitePackageColumnRecord(
                id = 99,
                alias = "operator-column",
                name = "Operator",
                preset = false,
            ),
        )
        val provisioner = SitePackageProvisioner(unusedLoader(), mapper)
        val sitePackage = foundationPackage()

        val first = provisioner.applyLoaded(sitePackage)
        assertEquals(2, first.inserted)
        assertEquals(0, first.conflicts)
        assertTrue(mapper.findByAlias("foundation-root")!!.preset)
        assertEquals(
            mapper.findByAlias("foundation-root")!!.id,
            mapper.findByAlias("foundation-child")!!.parentId,
        )

        val second = provisioner.applyLoaded(sitePackage)
        assertEquals(0, second.inserted)
        assertEquals(0, second.updated)
        assertEquals(2, second.unchanged)
        assertFalse(mapper.findByAlias("operator-column")!!.preset)
    }

    @Test
    fun `conflicts instead of taking over an operator-created stable alias`() {
        val mapper = FakeSitePackageColumnMapper()
        mapper.seed(
            SitePackageColumnRecord(
                id = 1,
                alias = "foundation-root",
                name = "Operator collision",
                preset = false,
            ),
        )
        val provisioner = SitePackageProvisioner(unusedLoader(), mapper)

        val report = provisioner.applyLoaded(foundationPackage())

        assertEquals(1, report.conflicts)
        assertEquals("Operator collision", mapper.findByAlias("foundation-root")!!.name)
        assertEquals(null, mapper.findByAlias("foundation-child"))
    }

    @Test
    fun `reconciles mutable fields of an existing preset without changing stable alias`() {
        val mapper = FakeSitePackageColumnMapper()
        mapper.seed(
            SitePackageColumnRecord(
                id = 1,
                alias = "foundation-root",
                name = "Old name",
                sortOrder = 999,
                preset = true,
            ),
        )
        val provisioner = SitePackageProvisioner(unusedLoader(), mapper)

        val packageDefinition = foundationPackage()
        val report = provisioner.applyLoaded(
            LoadedSitePackage(
                packageDefinition.manifest,
                listOf(packageDefinition.columns.first()),
            ),
        )

        assertEquals(1, report.updated)
        val current = mapper.findByAlias("foundation-root")!!
        assertEquals("Site Package Foundation Root", current.name)
        assertEquals(10, current.sortOrder)
        assertTrue(current.preset)
    }

    private fun foundationPackage() = LoadedSitePackage(
        SitePackageManifest(
            packageId = "foundation-fixture",
            schemaVersion = 1,
            version = "1",
            structure = listOf(
                SitePackageStructureFile(
                    kind = "columns",
                    path = "structure/columns.json",
                    sha256 = "0".repeat(64),
                ),
            ),
        ),
        listOf(
            SitePackageColumnDefinition(
                alias = "foundation-root",
                name = "Site Package Foundation Root",
                sortOrder = 10,
            ),
            SitePackageColumnDefinition(
                alias = "foundation-child",
                name = "Site Package Foundation Child",
                parentAlias = "foundation-root",
                sortOrder = 20,
            ),
        ),
    )

    private fun unusedLoader(): SitePackageLoader = SitePackageLoader(ObjectMapper())
}

private class FakeSitePackageColumnMapper : SitePackageColumnMapper {
    private val records = linkedMapOf<String, SitePackageColumnRecord>()
    private var nextId = 1L

    fun seed(record: SitePackageColumnRecord) {
        records[record.alias] = record.copy()
        nextId = maxOf(nextId, (record.id ?: 0) + 1)
    }

    override fun findByAlias(alias: String): SitePackageColumnRecord? = records[alias]?.copy()

    override fun insert(record: SitePackageColumnRecord): Int {
        if (record.alias in records) return 0
        record.id = nextId++
        record.preset = true
        records[record.alias] = record.copy()
        return 1
    }

    override fun updatePreset(record: SitePackageColumnRecord): Int {
        val current = records[record.alias] ?: return 0
        if (!current.preset || current.id != record.id) return 0
        records[record.alias] = record.copy(preset = true)
        return 1
    }
}
