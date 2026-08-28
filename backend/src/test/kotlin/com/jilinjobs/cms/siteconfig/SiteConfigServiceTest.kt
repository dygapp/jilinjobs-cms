package com.jilinjobs.cms.siteconfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

class SiteConfigServiceTest {
    @Test
    fun `accepts valid json and newly registered resource path keys`() {
        val mapper = FakeSiteConfigMapper(
            SiteConfigRecord("HOME_BANNERS", "[]", "JSON", "首页 Banner 配置"),
            SiteConfigRecord("HEADER_BANNER_PATH", "/static/home/header-banner.png", "RESOURCE_PATH", "头部 Banner"),
        )
        val service = SiteConfigService(mapper, ObjectMapper())

        val json = service.update("HOME_BANNERS", "[{\"image\":\"/static/home/a.png\"}]")
        val resource = service.update("HEADER_BANNER_PATH", "/static/home/new-header.png")

        assertEquals("[{\"image\":\"/static/home/a.png\"}]", json.value)
        assertEquals("/static/home/new-header.png", resource.value)
    }

    @Test
    fun `rejects malformed json instead of checking braces only`() {
        val mapper = FakeSiteConfigMapper(SiteConfigRecord("HOME_BANNERS", "[]", "JSON", "首页 Banner 配置"))
        val service = SiteConfigService(mapper, ObjectMapper())

        val error = assertThrows(SiteConfigValidationException::class.java) {
            service.update("HOME_BANNERS", "[{not-json}]")
        }

        assertEquals("配置项 HOME_BANNERS 必须是合法 JSON", error.message)
    }

    @Test
    fun `rejects scalar json and invalid resource paths`() {
        val mapper = FakeSiteConfigMapper(
            SiteConfigRecord("HOME_BANNERS", "[]", "JSON", "首页 Banner 配置"),
            SiteConfigRecord("LOGO_PATH", "", "RESOURCE_PATH", "Logo"),
        )
        val service = SiteConfigService(mapper, ObjectMapper())

        assertThrows(SiteConfigValidationException::class.java) { service.update("HOME_BANNERS", "true") }
        assertThrows(SiteConfigValidationException::class.java) { service.update("LOGO_PATH", "https://example.com/logo.png") }
    }

    @Test
    fun `rejects unknown config keys`() {
        val service = SiteConfigService(FakeSiteConfigMapper(), ObjectMapper())
        assertThrows(SiteConfigValidationException::class.java) { service.update("ARBITRARY_KEY", "value") }
    }
}

private class FakeSiteConfigMapper(vararg initial: SiteConfigRecord) : SiteConfigMapper {
    private val rows = initial.associateBy { it.configKey }.toMutableMap()

    override fun findAll(): List<SiteConfigRecord> = rows.values.sortedBy { it.configKey }
    override fun find(key: String): SiteConfigRecord? = rows[key]
    override fun update(key: String, value: String): Int {
        val row = rows[key] ?: return 0
        rows[key] = row.copy(configValue = value)
        return 1
    }
}
