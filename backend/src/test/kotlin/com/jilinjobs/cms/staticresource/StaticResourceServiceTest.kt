package com.jilinjobs.cms.staticresource

import com.jilinjobs.cms.siteconfig.SiteConfigMapper
import com.jilinjobs.cms.siteconfig.SiteConfigRecord
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.web.multipart.MultipartFile

class StaticResourceServiceTest {
    @TempDir
    lateinit var tempDir: Path

    private val png = byteArrayOf(
        0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
        0x00, 0x00, 0x00, 0x0d, 0x49, 0x48, 0x44, 0x52,
    )

    @Test
    fun `rejects an image extension when file content is not the declared format`() {
        val service = service()

        val error = assertThrows(StaticResourceValidationException::class.java) {
            service.upload("uploads/fake.png", BytesMultipartFile("fake.png", "plain text".toByteArray()), false)
        }

        assertTrue(error.message!!.contains("实际内容"))
        assertFalse(Files.exists(tempDir.resolve("uploads/fake.png")))
    }

    @Test
    fun `accepts valid signature and keeps ordinary delete restore workflow`() {
        val service = service()
        val uploaded = service.upload("uploads/real.png", BytesMultipartFile("real.png", png), false)

        assertEquals("uploads/real.png", uploaded.path)
        assertFalse(uploaded.protectedResource)

        val removed = service.delete(uploaded.path)
        assertFalse(Files.exists(tempDir.resolve(uploaded.path)))

        val restored = service.restore(removed.id)
        assertEquals(uploaded.path, restored.path)
        assertTrue(Files.isRegularFile(tempDir.resolve(uploaded.path)))
    }

    @Test
    fun `protects runtime baseline resource and site-config resource references from ordinary deletion`() {
        val mapper = FakeSiteConfigMapper(
            SiteConfigRecord("LOGO_PATH", "/static/brand/logo.png", "RESOURCE_PATH", "Logo"),
            SiteConfigRecord("HOME_BANNERS", "[{\"image\":\"/static/home/banner.png\"}]", "JSON", "Banner"),
        )
        val service = service(mapper)
        write("health/baseline.png", png)
        write("brand/logo.png", png)
        write("home/banner.png", png)
        write("home/ordinary.png", png)

        val health = service.list("health").single { it.name == "baseline.png" }
        val logo = service.list("brand").single { it.name == "logo.png" }
        val banner = service.list("home").single { it.name == "banner.png" }
        val ordinary = service.list("home").single { it.name == "ordinary.png" }

        assertTrue(health.protectedResource)
        assertTrue(logo.protectedResource)
        assertTrue(banner.protectedResource)
        assertFalse(ordinary.protectedResource)
        assertThrows(StaticResourceValidationException::class.java) { service.delete(health.path) }
        assertThrows(StaticResourceValidationException::class.java) { service.delete(logo.path) }
        assertThrows(StaticResourceValidationException::class.java) { service.delete(banner.path) }
        assertTrue(Files.isRegularFile(tempDir.resolve("health/baseline.png")))
    }

    @Test
    fun `explicit replacement remains allowed for a protected resource`() {
        val mapper = FakeSiteConfigMapper(SiteConfigRecord("LOGO_PATH", "/static/brand/logo.png", "RESOURCE_PATH", "Logo"))
        val service = service(mapper)
        write("brand/logo.png", png)

        val replaced = service.upload("brand/logo.png", BytesMultipartFile("logo.png", png + byteArrayOf(0x01)), true)

        assertTrue(replaced.protectedResource)
        assertTrue(Files.size(tempDir.resolve("brand/logo.png")) > png.size)
    }

    private fun service(mapper: SiteConfigMapper = FakeSiteConfigMapper()) = StaticResourceService(tempDir.toString(), mapper)

    private fun write(relative: String, bytes: ByteArray) {
        val path = tempDir.resolve(relative)
        Files.createDirectories(path.parent)
        Files.write(path, bytes)
    }
}

private class FakeSiteConfigMapper(vararg initial: SiteConfigRecord) : SiteConfigMapper {
    private val rows = initial.associateBy { it.configKey }.toMutableMap()
    override fun findAll(): List<SiteConfigRecord> = rows.values.toList()
    override fun find(key: String): SiteConfigRecord? = rows[key]
    override fun update(key: String, value: String): Int {
        val row = rows[key] ?: return 0
        rows[key] = row.copy(configValue = value)
        return 1
    }
}

private class BytesMultipartFile(
    private val filename: String,
    private val data: ByteArray,
) : MultipartFile {
    override fun getName(): String = "file"
    override fun getOriginalFilename(): String = filename
    override fun getContentType(): String? = null
    override fun isEmpty(): Boolean = data.isEmpty()
    override fun getSize(): Long = data.size.toLong()
    override fun getBytes(): ByteArray = data
    override fun getInputStream(): InputStream = ByteArrayInputStream(data)
    override fun transferTo(dest: File) {
        dest.parentFile?.mkdirs()
        dest.writeBytes(data)
    }
}
