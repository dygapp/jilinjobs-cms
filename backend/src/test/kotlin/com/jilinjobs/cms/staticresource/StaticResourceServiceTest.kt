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
    @TempDir lateinit var tempDir: Path
    private val png = byteArrayOf(0x89.toByte(),0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a,0x00,0x00,0x00,0x0d,0x49,0x48,0x44,0x52)

    @Test
    fun `rejects an image extension when file content is not the declared format`() {
        val service = service()
        val error = assertThrows(StaticResourceValidationException::class.java) { service.upload("uploads/fake.png", BytesMultipartFile("fake.png", "plain text".toByteArray()), false) }
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
    fun `protects runtime baseline site property and fixed ncss engineering asset`() {
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
    fun `explicit replacement remains allowed for a protected resource`() {
        val mapper = StaticFakeSiteConfigMapper(property("LOGO_PATH", "/static/brand/logo.png", "RESOURCE_PATH"))
        val service = service(mapper)
        write("brand/logo.png", png)
        val replaced = service.upload("brand/logo.png", BytesMultipartFile("logo.png", png + byteArrayOf(0x01)), true)
        assertTrue(replaced.protectedResource)
        assertTrue(Files.size(tempDir.resolve("brand/logo.png")) > png.size)
    }

    private fun service(mapper: SiteConfigMapper = StaticFakeSiteConfigMapper()) = StaticResourceService(tempDir.toString(), mapper)
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

private class BytesMultipartFile(private val filename:String,private val data:ByteArray):MultipartFile{
    override fun getName()="file";override fun getOriginalFilename()=filename;override fun getContentType():String?=null;override fun isEmpty()=data.isEmpty();override fun getSize()=data.size.toLong();override fun getBytes()=data;override fun getInputStream():InputStream=ByteArrayInputStream(data);override fun transferTo(dest:File){dest.parentFile?.mkdirs();dest.writeBytes(data)}
}
