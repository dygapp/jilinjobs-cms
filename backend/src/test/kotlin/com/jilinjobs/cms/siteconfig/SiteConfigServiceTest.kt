package com.jilinjobs.cms.siteconfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

class SiteConfigServiceTest {
    @Test
    fun `accepts runtime-defined json and resource properties`() {
        val mapper = FakeSiteConfigMapper(
            record("CUSTOM_JSON", "JSON", "{}"),
            record("HEADER_BANNER_PATH", "RESOURCE_PATH", "/static/home/header-banner.png"),
        )
        val service = SiteConfigService(mapper, ObjectMapper())

        val json = service.update("CUSTOM_JSON", "{\"enabled\":true}")
        val resource = service.update("HEADER_BANNER_PATH", "/static/home/new-header.png")

        assertEquals("{\"enabled\":true}", json.value)
        assertEquals("/static/home/new-header.png", resource.value)
    }

    @Test
    fun `creates custom property without compile-time key whitelist`() {
        val service = SiteConfigService(FakeSiteConfigMapper(), ObjectMapper())
        val created = service.create(SiteConfigDraft("SUPPORT_EMAIL", "支持邮箱", "CONTACT", "help@example.com", "TEXT"))
        assertEquals("SUPPORT_EMAIL", created.key)
        assertEquals("支持邮箱", created.name)
    }

    @Test
    fun `rejects malformed json and invalid resource paths`() {
        val mapper = FakeSiteConfigMapper(record("CUSTOM_JSON", "JSON", "{}"), record("LOGO_PATH", "RESOURCE_PATH", ""))
        val service = SiteConfigService(mapper, ObjectMapper())
        assertThrows(SiteConfigValidationException::class.java) { service.update("CUSTOM_JSON", "[{not-json}]") }
        assertThrows(SiteConfigValidationException::class.java) { service.update("LOGO_PATH", "https://example.com/logo.png") }
    }

    @Test
    fun `validates url and boolean property types`() {
        val mapper = FakeSiteConfigMapper(record("SERVICE_URL", "URL", "/page/about"), record("FEATURE_ENABLED", "BOOLEAN", "true"))
        val service = SiteConfigService(mapper, ObjectMapper())
        assertEquals("https://example.com/path", service.update("SERVICE_URL", "https://example.com/path").value)
        assertThrows(SiteConfigValidationException::class.java) { service.update("SERVICE_URL", "javascript:alert(1)") }
        assertThrows(SiteConfigValidationException::class.java) { service.update("FEATURE_ENABLED", "yes") }
    }

    private fun record(key: String, type: String, value: String) = SiteConfigRecord(
        configKey = key,
        propertyName = key,
        groupCode = "GENERAL",
        configValue = value,
        valueType = type,
        description = key,
    )
}

private class FakeSiteConfigMapper(vararg initial: SiteConfigRecord) : SiteConfigMapper {
    private val rows = initial.associateBy { it.configKey }.toMutableMap()
    override fun findAll(): List<SiteConfigRecord> = rows.values.sortedBy { it.configKey }
    override fun findEnabled(): List<SiteConfigRecord> = rows.values.filter { it.enabled }.sortedBy { it.configKey }
    override fun find(key: String): SiteConfigRecord? = rows[key]
    override fun insert(record: SiteConfigRecord): Int { if (rows.containsKey(record.configKey)) return 0; rows[record.configKey] = record; return 1 }
    override fun updateDefinition(record: SiteConfigRecord): Int { if (!rows.containsKey(record.configKey)) return 0; rows[record.configKey] = record; return 1 }
    override fun update(key: String, value: String): Int { val row = rows[key] ?: return 0; rows[key] = row.copy(configValue = value); return 1 }
    override fun delete(key: String): Int = if (rows.remove(key) != null) 1 else 0
}
