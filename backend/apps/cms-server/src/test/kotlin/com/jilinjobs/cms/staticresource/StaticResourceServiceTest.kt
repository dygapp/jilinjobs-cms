package com.jilinjobs.cms.staticresource

import com.jilinjobs.cms.siteconfig.SiteConfigMapper
import com.jilinjobs.cms.siteconfig.SiteConfigRecord
import com.jilinjobs.cms.resource.UploadContent
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class StaticResourceServiceTest {
    @TempDir lateinit var tempDir: Path
    private val png = byteArrayOf(0x89.toByte(),0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a,0x00,0x00,0x00,0x0d,0x49,0x48,0x44,0x52)

    @Test
    fun `rejects an image extension when file content is not the declared format`() {
        val service = service()
        val error = assertThrows(StaticResourceValidationException::class.java) { service.upload("uploads/fake.png", bytesUpload("fake.png", "plain text".toByteArray()), false) }
        assertTrue(error.message!!.contains("实际内容"))
        assertFalse(Files.exists(tempDir.resolve("uploads/fake.png")))
    }

    @Test
    fun `rejects an empty upload`() {
        val error = assertThrows(StaticResourceValidationException::class.java) {
            service().upload("uploads/empty.png", bytesUpload("empty.png", byteArrayOf()), false)
        }
        assertTrue(error.message!!.contains("不能为空"))
        assertFalse(Files.exists(tempDir.resolve("uploads/empty.png")))
    }

    @Test
    fun `reopens content for office signature and final copy`() {
        val bytes = java.io.ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("word/document.xml"))
                zip.write("<document/>".toByteArray())
                zip.closeEntry()
            }
            output.toByteArray()
        }
        val opens = AtomicInteger()
        val upload = UploadContent("sample.docx", "application/octet-stream", bytes.size.toLong()) {
            opens.incrementAndGet()
            ByteArrayInputStream(bytes)
        }

        service().upload("uploads/sample.docx", upload, false)

        assertTrue(Files.readAllBytes(tempDir.resolve("uploads/sample.docx")).contentEquals(bytes))
        assertEquals(3, opens.get())
    }

    @Test
    fun `accepts valid signature and keeps ordinary delete restore workflow`() {
        val service = service()
        val uploaded = service.upload("uploads/real.png", bytesUpload("real.png", png), false)
        assertEquals("uploads/real.png", uploaded.path)
        assertFalse(uploaded.protectedResource)
        val removed = service.delete(uploaded.path)
        assertFalse(Files.exists(tempDir.resolve(uploaded.path)))
        val restored = service.restore(removed.id)
        assertEquals(uploaded.path, restored.path)
        assertTrue(Files.isRegularFile(tempDir.resolve(uploaded.path)))
    }

    @Test
    fun `protects configured baseline and runtime referenced resources`() {
        val mapper = StaticFakeSiteConfigMapper(property("LOGO_PATH", "/static/brand/logo.png", "RESOURCE_PATH"))
        val service = service(mapper)
        write("health/baseline.png", png)
        write("brand/logo.png", png)
        write("home/ncss-logo.png", png)
        write("home/ordinary.png", png)

        assertTrue(service.list("health").single { it.name == "baseline.png" }.protectedResource)
        assertTrue(service.list("brand").single { it.name == "logo.png" }.protectedResource)
        assertTrue(service.list("home").single { it.name == "ncss-logo.png" }.protectedResource)
        assertFalse(service.list("home").single { it.name == "ordinary.png" }.protectedResource)
        assertThrows(StaticResourceValidationException::class.java) { service.delete("home/ncss-logo.png") }
    }

    @Test
    fun `configured baseline is not hard coded by service`() {
        val service = service(protectedResources = "custom/protected.png")
        write("custom/protected.png", png)
        write("health/baseline.png", png)

        assertTrue(service.list("custom").single().protectedResource)
        assertFalse(service.list("health").single().protectedResource)
    }

    @Test
    fun `explicit replacement remains allowed for a protected resource`() {
        val mapper = StaticFakeSiteConfigMapper(property("LOGO_PATH", "/static/brand/logo.png", "RESOURCE_PATH"))
        val service = service(mapper)
        write("brand/logo.png", png)
        val replaced = service.upload("brand/logo.png", bytesUpload("logo.png", png + byteArrayOf(0x01)), true)
        assertTrue(replaced.protectedResource)
        assertTrue(Files.size(tempDir.resolve("brand/logo.png")) > png.size)
    }

    private fun service(
        mapper: SiteConfigMapper = StaticFakeSiteConfigMapper(),
        protectedResources: String = "health/baseline.png,home/ncss-logo.png",
    ) = StaticResourceService(tempDir.toString(), mapper, protectedResourcesText = protectedResources)
    private fun write(relative: String, bytes: ByteArray) { val path=tempDir.resolve(relative);Files.createDirectories(path.parent);Files.write(path,bytes) }
    private fun property(key:String,value:String,type:String)=SiteConfigRecord(configKey=key,propertyName=key,configValue=value,valueType=type,description=key)
}

private class StaticFakeSiteConfigMapper(vararg initial: SiteConfigRecord) : SiteConfigMapper {
    private val rows = initial.associateBy { it.configKey }.toMutableMap()
    override fun findAll() = rows.values.toList()
    override fun findEnabled() = rows.values.filter { it.enabled }
    override fun find(key: String) = rows[key]
    override fun insert(record: SiteConfigRecord): Int { rows[record.configKey]=record;return 1 }
    override fun updateDefinition(record: SiteConfigRecord): Int { rows[record.configKey]=record;return 1 }
    override fun update(key: String, value: String): Int { val row=rows[key]?:return 0;rows[key]=row.copy(configValue=value);return 1 }
    override fun delete(key: String): Int = if(rows.remove(key)!=null)1 else 0
}

private fun bytesUpload(filename: String, data: ByteArray) =
    UploadContent(filename, null, data.size.toLong()) { ByteArrayInputStream(data) }
