package com.jilinjobs.cms.navigation

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.apache.ibatis.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

data class NavigationLocation(
    val id: Long,
    val code: String,
    val name: String,
    val description: String,
    val sortOrder: Int,
    val enabled: Boolean,
    val system: Boolean,
)

data class NavigationLocationDraft(
    val code: String,
    val name: String,
    val description: String = "",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val system: Boolean = false,
)

data class NavigationLocationRecord(
    var id: Long? = null,
    var code: String = "",
    var name: String = "",
    var description: String = "",
    var sortOrder: Int = 0,
    var enabled: Boolean = true,
    var system: Boolean = false,
)

class NavigationLocationNotFoundException(code: String) : RuntimeException("导航位置不存在：$code")

@Mapper
interface NavigationLocationMapper {
    @Select("SELECT id,code,name,description,sort_order,enabled,system FROM cms_navigation_location ORDER BY sort_order,id")
    fun findAll(): List<NavigationLocationRecord>

    @Select("SELECT id,code,name,description,sort_order,enabled,system FROM cms_navigation_location WHERE code=#{code}")
    fun findByCode(@Param("code") code: String): NavigationLocationRecord?

    @Insert("INSERT INTO cms_navigation_location(code,name,description,sort_order,enabled,system) VALUES(#{code},#{name},#{description},#{sortOrder},#{enabled},#{system})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(record: NavigationLocationRecord): Int

    @Update("UPDATE cms_navigation_location SET name=#{name},description=#{description},sort_order=#{sortOrder},enabled=#{enabled},system=#{system} WHERE code=#{code}")
    fun update(record: NavigationLocationRecord): Int

    @Delete("DELETE FROM cms_navigation_location WHERE code=#{code}")
    fun delete(@Param("code") code: String): Int
}

@Service
class NavigationLocationService(
    private val mapper: NavigationLocationMapper,
    private val navigationMapper: NavigationMapper,
) {
    @Transactional(readOnly = true)
    fun list(): List<NavigationLocation> = mapper.findAll().map { it.model() }

    @Transactional
    fun create(draft: NavigationLocationDraft): NavigationLocation {
        val normalized = normalize(draft)
        if (mapper.findByCode(normalized.code) != null) throw NavigationValidationException("导航位置 Code 已存在：${normalized.code}")
        val record = normalized.record()
        mapper.insert(record)
        return record.model()
    }

    @Transactional
    fun update(code: String, draft: NavigationLocationDraft): NavigationLocation {
        val normalizedCode = normalizeCode(code)
        mapper.findByCode(normalizedCode) ?: throw NavigationLocationNotFoundException(normalizedCode)
        val normalized = normalize(draft.copy(code = normalizedCode))
        val record = normalized.record()
        mapper.update(record)
        return mapper.findByCode(normalizedCode)!!.model()
    }

    @Transactional
    fun delete(code: String) {
        val normalized = normalizeCode(code)
        mapper.findByCode(normalized) ?: throw NavigationLocationNotFoundException(normalized)
        if (navigationMapper.countByPosition(normalized) > 0) throw NavigationValidationException("该导航位置仍包含导航条目，不能删除")
        mapper.delete(normalized)
    }

    private fun normalize(draft: NavigationLocationDraft): NavigationLocationDraft {
        val code = normalizeCode(draft.code)
        val name = draft.name.trim()
        if (name.isBlank()) throw NavigationValidationException("导航位置名称不能为空")
        if (name.length > 100) throw NavigationValidationException("导航位置名称不能超过 100 个字符")
        return draft.copy(code = code, name = name, description = draft.description.trim())
    }

    private fun normalizeCode(raw: String): String {
        val code = raw.trim().uppercase()
        if (!code.matches(Regex("[A-Z][A-Z0-9_]{1,49}"))) throw NavigationValidationException("导航位置 Code 必须由大写字母、数字和下划线组成")
        return code
    }

    private fun NavigationLocationDraft.record() = NavigationLocationRecord(
        code = code, name = name, description = description, sortOrder = sortOrder, enabled = enabled, system = system,
    )

    private fun NavigationLocationRecord.model() = NavigationLocation(
        requireNotNull(id), code, name, description, sortOrder, enabled, system,
    )
}

@RestController
@RequestMapping("/api/admin/navigation-locations")
class AdminNavigationLocationController(private val service: NavigationLocationService) {
    @GetMapping fun list() = service.list()
    @PostMapping fun create(@Valid @RequestBody request: SaveNavigationLocationRequest) =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.draft()))
    @PutMapping("/{code}") fun update(@PathVariable code: String, @Valid @RequestBody request: SaveNavigationLocationRequest) =
        service.update(code, request.draft())
    @DeleteMapping("/{code}") fun delete(@PathVariable code: String): ResponseEntity<Void> {
        service.delete(code)
        return ResponseEntity.noContent().build()
    }
}

data class SaveNavigationLocationRequest(
    @field:NotBlank @field:Size(max = 50) val code: String,
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:Size(max = 255) val description: String = "",
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val system: Boolean = false,
) {
    fun draft() = NavigationLocationDraft(code, name, description, sortOrder, enabled, system)
}
