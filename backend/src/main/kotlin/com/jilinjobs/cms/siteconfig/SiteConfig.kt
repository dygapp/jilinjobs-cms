package com.jilinjobs.cms.siteconfig

import java.net.URI
import org.apache.ibatis.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.ObjectMapper

data class SiteConfigItem(
    val key: String,
    val name: String,
    val groupCode: String,
    val value: String,
    val valueType: String,
    val description: String,
    val sortOrder: Int,
    val required: Boolean,
    val system: Boolean,
    val enabled: Boolean,
)

data class SiteConfigDraft(
    val key: String,
    val name: String,
    val groupCode: String = "GENERAL",
    val value: String = "",
    val valueType: String = "TEXT",
    val description: String = "",
    val sortOrder: Int = 0,
    val required: Boolean = false,
    val system: Boolean = false,
    val enabled: Boolean = true,
)

class SiteConfigValidationException(message: String) : RuntimeException(message)
class SiteConfigNotFoundException(key: String) : RuntimeException("网站属性不存在：$key")

@Mapper
interface SiteConfigMapper {
    @Select("SELECT config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system,enabled FROM cms_site_config ORDER BY group_code,sort_order,config_key")
    fun findAll(): List<SiteConfigRecord>

    @Select("SELECT config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system,enabled FROM cms_site_config WHERE enabled=1 ORDER BY group_code,sort_order,config_key")
    fun findEnabled(): List<SiteConfigRecord>

    @Select("SELECT config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system,enabled FROM cms_site_config WHERE config_key=#{key}")
    fun find(@Param("key") key: String): SiteConfigRecord?

    @Insert("INSERT INTO cms_site_config(config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system,enabled) VALUES(#{configKey},#{propertyName},#{groupCode},#{configValue},#{valueType},#{description},#{sortOrder},#{required},#{system},#{enabled})")
    fun insert(record: SiteConfigRecord): Int

    @Update("UPDATE cms_site_config SET property_name=#{propertyName},group_code=#{groupCode},config_value=#{configValue},value_type=#{valueType},description=#{description},sort_order=#{sortOrder},required=#{required},system=#{system},enabled=#{enabled} WHERE config_key=#{configKey}")
    fun updateDefinition(record: SiteConfigRecord): Int

    @Update("UPDATE cms_site_config SET config_value=#{value} WHERE config_key=#{key}")
    fun update(@Param("key") key: String, @Param("value") value: String): Int

    @Delete("DELETE FROM cms_site_config WHERE config_key=#{key}")
    fun delete(@Param("key") key: String): Int
}

data class SiteConfigRecord(
    var configKey: String = "",
    var propertyName: String = "",
    var groupCode: String = "GENERAL",
    var configValue: String = "",
    var valueType: String = "TEXT",
    var description: String = "",
    var sortOrder: Int = 0,
    var required: Boolean = false,
    var system: Boolean = false,
    var enabled: Boolean = true,
)

@Service
class SiteConfigService(
    private val mapper: SiteConfigMapper,
    private val objectMapper: ObjectMapper,
) {
    private val allowedTypes = setOf("TEXT", "RESOURCE_PATH", "JSON", "URL", "BOOLEAN")

    @Transactional(readOnly = true)
    fun list() = mapper.findAll().map { it.item() }

    @Transactional(readOnly = true)
    fun listPublic() = mapper.findEnabled().map { it.item() }

    @Transactional
    fun create(draft: SiteConfigDraft): SiteConfigItem {
        val normalized = normalize(draft)
        if (mapper.find(normalized.key) != null) throw SiteConfigValidationException("网站属性 Key 已存在：${normalized.key}")
        val record = normalized.record()
        mapper.insert(record)
        return record.item()
    }

    @Transactional
    fun updateDefinition(key: String, draft: SiteConfigDraft): SiteConfigItem {
        val normalizedKey = normalizeKey(key)
        mapper.find(normalizedKey) ?: throw SiteConfigNotFoundException(normalizedKey)
        val normalized = normalize(draft.copy(key = normalizedKey))
        mapper.updateDefinition(normalized.record())
        return mapper.find(normalizedKey)!!.item()
    }

    @Transactional
    fun update(key: String, value: String): SiteConfigItem {
        val normalizedKey = normalizeKey(key)
        val row = mapper.find(normalizedKey) ?: throw SiteConfigNotFoundException(normalizedKey)
        validateValue(row.valueType, value, row.required, normalizedKey)
        mapper.update(normalizedKey, value)
        return mapper.find(normalizedKey)!!.item()
    }

    @Transactional
    fun delete(key: String) {
        val normalizedKey = normalizeKey(key)
        mapper.find(normalizedKey) ?: throw SiteConfigNotFoundException(normalizedKey)
        mapper.delete(normalizedKey)
    }

    private fun normalize(draft: SiteConfigDraft): SiteConfigDraft {
        val key = normalizeKey(draft.key)
        val name = draft.name.trim()
        if (name.isBlank()) throw SiteConfigValidationException("网站属性名称不能为空")
        if (name.length > 100) throw SiteConfigValidationException("网站属性名称不能超过 100 个字符")
        val groupCode = draft.groupCode.trim().uppercase().ifBlank { "GENERAL" }
        if (!groupCode.matches(Regex("[A-Z][A-Z0-9_]{1,49}"))) throw SiteConfigValidationException("属性分组 Code 格式不正确")
        val type = draft.valueType.trim().uppercase()
        if (type !in allowedTypes) throw SiteConfigValidationException("不支持的网站属性类型：$type")
        validateValue(type, draft.value, draft.required, key)
        return draft.copy(
            key = key,
            name = name,
            groupCode = groupCode,
            valueType = type,
            description = draft.description.trim(),
        )
    }

    private fun normalizeKey(raw: String): String {
        val key = raw.trim().uppercase()
        if (!key.matches(Regex("[A-Z][A-Z0-9_]{1,99}"))) throw SiteConfigValidationException("网站属性 Key 必须由大写字母、数字和下划线组成")
        return key
    }

    private fun validateValue(type: String, value: String, required: Boolean, key: String) {
        val trimmed = value.trim()
        if (required && trimmed.isBlank()) throw SiteConfigValidationException("网站属性 $key 不能为空")
        if (trimmed.isBlank()) return
        when (type) {
            "JSON" -> {
                val node = runCatching { objectMapper.readTree(value) }.getOrElse { throw SiteConfigValidationException("网站属性 $key 必须是合法 JSON") }
                if (node == null || (!node.isArray && !node.isObject)) throw SiteConfigValidationException("网站属性 $key 必须是 JSON 数组或对象")
            }
            "RESOURCE_PATH" -> if (!trimmed.startsWith("/static/")) throw SiteConfigValidationException("网站属性 $key 必须使用 /static/ 资源路径")
            "URL" -> validateUrl(trimmed, key)
            "BOOLEAN" -> if (trimmed.lowercase() !in setOf("true", "false")) throw SiteConfigValidationException("网站属性 $key 必须是 true 或 false")
        }
    }

    private fun validateUrl(value: String, key: String) {
        if (value.startsWith("/") && !value.startsWith("//")) return
        val uri = runCatching { URI(value) }.getOrElse { throw SiteConfigValidationException("网站属性 $key URL 格式不正确") }
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) throw SiteConfigValidationException("网站属性 $key 必须是站内路径或 HTTP(S) 地址")
    }

    private fun SiteConfigDraft.record() = SiteConfigRecord(key, name, groupCode, value, valueType, description, sortOrder, required, system, enabled)
    private fun SiteConfigRecord.item() = SiteConfigItem(configKey, propertyName, groupCode, configValue, valueType, description, sortOrder, required, system, enabled)
}

@RestController
@RequestMapping("/api/admin/site-config")
class AdminSiteConfigController(private val service: SiteConfigService) {
    @GetMapping fun list() = service.list()
    @PostMapping fun create(@RequestBody request: SaveSiteConfigRequest) = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.draft()))
    @PutMapping("/{key}") fun updateValue(@PathVariable key: String, @RequestBody request: SiteConfigUpdateRequest) = service.update(key, request.value)
    @PutMapping("/{key}/definition") fun updateDefinition(@PathVariable key: String, @RequestBody request: SaveSiteConfigRequest) = service.updateDefinition(key, request.draft())
    @DeleteMapping("/{key}") fun delete(@PathVariable key: String): ResponseEntity<Void> {
        service.delete(key)
        return ResponseEntity.noContent().build()
    }
}

@RestController
@RequestMapping("/api/public/site-config")
class PublicSiteConfigController(private val service: SiteConfigService) {
    @GetMapping fun list() = service.listPublic()
}

data class SiteConfigUpdateRequest(val value: String)

data class SaveSiteConfigRequest(
    val key: String,
    val name: String,
    val groupCode: String = "GENERAL",
    val value: String = "",
    val valueType: String = "TEXT",
    val description: String = "",
    val sortOrder: Int = 0,
    val required: Boolean = false,
    val system: Boolean = false,
    val enabled: Boolean = true,
) {
    fun draft() = SiteConfigDraft(key, name, groupCode, value, valueType, description, sortOrder, required, system, enabled)
}
