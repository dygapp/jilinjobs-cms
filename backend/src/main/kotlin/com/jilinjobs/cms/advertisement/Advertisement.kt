package com.jilinjobs.cms.advertisement

import java.net.URI
import java.time.LocalDateTime
import org.apache.ibatis.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

data class AdvertisementSlot(val id: Long, val code: String, val name: String, val description: String, val sortOrder: Int, val enabled: Boolean, val system: Boolean)
data class Advertisement(val id: Long, val slotId: Long, val title: String, val imagePath: String, val url: String?, val openMode: String, val startAt: LocalDateTime?, val endAt: LocalDateTime?, val sortOrder: Int, val enabled: Boolean)
data class PublicAdvertisementSlot(val id: Long, val code: String, val name: String, val advertisements: List<Advertisement>)

data class AdvertisementSlotDraft(val code: String, val name: String, val description: String = "", val sortOrder: Int = 0, val enabled: Boolean = true, val system: Boolean = false)
data class AdvertisementDraft(val title: String, val imagePath: String, val url: String? = null, val openMode: String = "DEFAULT", val startAt: LocalDateTime? = null, val endAt: LocalDateTime? = null, val sortOrder: Int = 0, val enabled: Boolean = true)

class AdvertisementValidationException(message: String) : RuntimeException(message)
class AdvertisementSlotNotFoundException(value: String) : RuntimeException("广告位不存在：$value")
class AdvertisementNotFoundException(id: Long) : RuntimeException("广告不存在：$id")

data class AdvertisementSlotRecord(var id: Long? = null, var code: String = "", var name: String = "", var description: String = "", var sortOrder: Int = 0, var enabled: Boolean = true, var system: Boolean = false)
data class AdvertisementRecord(var id: Long? = null, var slotId: Long = 0, var title: String = "", var imagePath: String = "", var url: String? = null, var openMode: String = "DEFAULT", var startAt: LocalDateTime? = null, var endAt: LocalDateTime? = null, var sortOrder: Int = 0, var enabled: Boolean = true)

@Mapper
interface AdvertisementMapper {
    @Select("SELECT id,code,name,description,sort_order,enabled,system FROM cms_ad_slot ORDER BY sort_order,id") fun findSlots(): List<AdvertisementSlotRecord>
    @Select("SELECT id,code,name,description,sort_order,enabled,system FROM cms_ad_slot WHERE enabled=1 ORDER BY sort_order,id") fun findEnabledSlots(): List<AdvertisementSlotRecord>
    @Select("SELECT id,code,name,description,sort_order,enabled,system FROM cms_ad_slot WHERE id=#{id}") fun findSlot(@Param("id") id: Long): AdvertisementSlotRecord?
    @Select("SELECT id,code,name,description,sort_order,enabled,system FROM cms_ad_slot WHERE code=#{code}") fun findSlotByCode(@Param("code") code: String): AdvertisementSlotRecord?
    @Insert("INSERT INTO cms_ad_slot(code,name,description,sort_order,enabled,system) VALUES(#{code},#{name},#{description},#{sortOrder},#{enabled},#{system})") @Options(useGeneratedKeys = true, keyProperty = "id") fun insertSlot(record: AdvertisementSlotRecord): Int
    @Update("UPDATE cms_ad_slot SET name=#{name},description=#{description},sort_order=#{sortOrder},enabled=#{enabled},system=#{system} WHERE id=#{id}") fun updateSlot(record: AdvertisementSlotRecord): Int
    @Delete("DELETE FROM cms_ad_slot WHERE id=#{id}") fun deleteSlot(@Param("id") id: Long): Int

    @Select("SELECT id,slot_id,title,image_path,url,open_mode,start_at,end_at,sort_order,enabled FROM cms_advertisement WHERE slot_id=#{slotId} ORDER BY sort_order,id") fun findAds(@Param("slotId") slotId: Long): List<AdvertisementRecord>
    @Select("SELECT id,slot_id,title,image_path,url,open_mode,start_at,end_at,sort_order,enabled FROM cms_advertisement WHERE slot_id=#{slotId} AND enabled=1 AND (start_at IS NULL OR start_at<=CURRENT_TIMESTAMP) AND (end_at IS NULL OR end_at>CURRENT_TIMESTAMP) ORDER BY sort_order,id") fun findActiveAds(@Param("slotId") slotId: Long): List<AdvertisementRecord>
    @Select("SELECT id,slot_id,title,image_path,url,open_mode,start_at,end_at,sort_order,enabled FROM cms_advertisement WHERE id=#{id}") fun findAd(@Param("id") id: Long): AdvertisementRecord?
    @Insert("INSERT INTO cms_advertisement(slot_id,title,image_path,url,open_mode,start_at,end_at,sort_order,enabled) VALUES(#{slotId},#{title},#{imagePath},#{url},#{openMode},#{startAt},#{endAt},#{sortOrder},#{enabled})") @Options(useGeneratedKeys = true, keyProperty = "id") fun insertAd(record: AdvertisementRecord): Int
    @Update("UPDATE cms_advertisement SET title=#{title},image_path=#{imagePath},url=#{url},open_mode=#{openMode},start_at=#{startAt},end_at=#{endAt},sort_order=#{sortOrder},enabled=#{enabled} WHERE id=#{id}") fun updateAd(record: AdvertisementRecord): Int
    @Delete("DELETE FROM cms_advertisement WHERE id=#{id}") fun deleteAd(@Param("id") id: Long): Int
    @Select("SELECT image_path FROM cms_advertisement a JOIN cms_ad_slot s ON s.id=a.slot_id WHERE s.enabled=1 AND a.enabled=1 AND a.image_path LIKE '/static/%'") fun findReferencedImages(): List<String>
}

@Service
class AdvertisementService(private val mapper: AdvertisementMapper) {
    private val openModes = setOf("DEFAULT", "SAME_WINDOW", "NEW_WINDOW")

    @Transactional(readOnly = true) fun slots() = mapper.findSlots().map { it.model() }
    @Transactional(readOnly = true) fun ads(slotId: Long): List<Advertisement> { requireSlot(slotId); return mapper.findAds(slotId).map { it.model() } }
    @Transactional(readOnly = true) fun publicSlots() = mapper.findEnabledSlots().map { row -> PublicAdvertisementSlot(requireNotNull(row.id), row.code, row.name, mapper.findActiveAds(requireNotNull(row.id)).map { it.model() }) }

    @Transactional
    fun createSlot(draft: AdvertisementSlotDraft): AdvertisementSlot {
        val normalized = normalizeSlot(draft)
        if (mapper.findSlotByCode(normalized.code) != null) throw AdvertisementValidationException("广告位 Code 已存在：${normalized.code}")
        val row = normalized.record()
        mapper.insertSlot(row)
        return row.model()
    }

    @Transactional
    fun updateSlot(id: Long, draft: AdvertisementSlotDraft): AdvertisementSlot {
        val current = requireSlot(id)
        val normalized = normalizeSlot(draft.copy(code = current.code))
        mapper.updateSlot(normalized.record(id))
        return mapper.findSlot(id)!!.model()
    }

    @Transactional fun deleteSlot(id: Long) { requireSlot(id); mapper.deleteSlot(id) }

    @Transactional
    fun createAd(slotId: Long, draft: AdvertisementDraft): Advertisement {
        requireSlot(slotId)
        val row = normalizeAd(draft).record(slotId)
        mapper.insertAd(row)
        return row.model()
    }

    @Transactional
    fun updateAd(slotId: Long, id: Long, draft: AdvertisementDraft): Advertisement {
        requireSlot(slotId)
        val current = mapper.findAd(id) ?: throw AdvertisementNotFoundException(id)
        if (current.slotId != slotId) throw AdvertisementValidationException("广告不属于当前广告位")
        val row = normalizeAd(draft).record(slotId, id)
        mapper.updateAd(row)
        return mapper.findAd(id)!!.model()
    }

    @Transactional
    fun deleteAd(slotId: Long, id: Long) {
        requireSlot(slotId)
        val current = mapper.findAd(id) ?: throw AdvertisementNotFoundException(id)
        if (current.slotId != slotId) throw AdvertisementValidationException("广告不属于当前广告位")
        mapper.deleteAd(id)
    }

    private fun requireSlot(id: Long) = mapper.findSlot(id) ?: throw AdvertisementSlotNotFoundException(id.toString())

    private fun normalizeSlot(draft: AdvertisementSlotDraft): AdvertisementSlotDraft {
        val code = draft.code.trim().uppercase()
        if (!code.matches(Regex("[A-Z][A-Z0-9_]{1,99}"))) throw AdvertisementValidationException("广告位 Code 格式不正确")
        val name = draft.name.trim()
        if (name.isBlank()) throw AdvertisementValidationException("广告位名称不能为空")
        return draft.copy(code = code, name = name, description = draft.description.trim())
    }

    private fun normalizeAd(draft: AdvertisementDraft): AdvertisementDraft {
        val title = draft.title.trim()
        if (title.isBlank()) throw AdvertisementValidationException("广告标题不能为空")
        val image = draft.imagePath.trim()
        if (!image.startsWith("/static/")) throw AdvertisementValidationException("广告图片必须使用 /static/ 资源路径")
        val openMode = draft.openMode.trim().uppercase()
        if (openMode !in openModes) throw AdvertisementValidationException("广告打开方式不正确")
        val url = draft.url?.trim()?.takeIf { it.isNotBlank() }
        if (url != null) validateUrl(url)
        if (draft.startAt != null && draft.endAt != null && !draft.endAt.isAfter(draft.startAt)) throw AdvertisementValidationException("广告结束时间必须晚于开始时间")
        return draft.copy(title = title, imagePath = image, url = url, openMode = openMode)
    }

    private fun validateUrl(value: String) {
        if (value.startsWith("/") && !value.startsWith("//")) return
        val uri = runCatching { URI(value) }.getOrElse { throw AdvertisementValidationException("广告 URL 格式不正确") }
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) throw AdvertisementValidationException("广告 URL 必须是站内路径或 HTTP(S) 地址")
    }

    private fun AdvertisementSlotDraft.record(id: Long? = null) = AdvertisementSlotRecord(id, code, name, description, sortOrder, enabled, system)
    private fun AdvertisementDraft.record(slotId: Long, id: Long? = null) = AdvertisementRecord(id, slotId, title, imagePath, url, openMode, startAt, endAt, sortOrder, enabled)
    private fun AdvertisementSlotRecord.model() = AdvertisementSlot(requireNotNull(id), code, name, description, sortOrder, enabled, system)
    private fun AdvertisementRecord.model() = Advertisement(requireNotNull(id), slotId, title, imagePath, url, openMode, startAt, endAt, sortOrder, enabled)
}

@RestController
@RequestMapping("/api/admin/advertisements")
class AdminAdvertisementController(private val service: AdvertisementService) {
    @GetMapping("/slots") fun slots() = service.slots()
    @PostMapping("/slots") fun createSlot(@RequestBody request: SaveAdvertisementSlotRequest) = ResponseEntity.status(HttpStatus.CREATED).body(service.createSlot(request.draft()))
    @PutMapping("/slots/{id}") fun updateSlot(@PathVariable id: Long, @RequestBody request: SaveAdvertisementSlotRequest) = service.updateSlot(id, request.draft())
    @DeleteMapping("/slots/{id}") fun deleteSlot(@PathVariable id: Long): ResponseEntity<Void> { service.deleteSlot(id); return ResponseEntity.noContent().build() }
    @GetMapping("/slots/{id}/items") fun ads(@PathVariable id: Long) = service.ads(id)
    @PostMapping("/slots/{id}/items") fun createAd(@PathVariable id: Long, @RequestBody request: SaveAdvertisementRequest) = ResponseEntity.status(HttpStatus.CREATED).body(service.createAd(id, request.draft()))
    @PutMapping("/slots/{slotId}/items/{adId}") fun updateAd(@PathVariable slotId: Long, @PathVariable adId: Long, @RequestBody request: SaveAdvertisementRequest) = service.updateAd(slotId, adId, request.draft())
    @DeleteMapping("/slots/{slotId}/items/{adId}") fun deleteAd(@PathVariable slotId: Long, @PathVariable adId: Long): ResponseEntity<Void> { service.deleteAd(slotId, adId); return ResponseEntity.noContent().build() }
}

@RestController
@RequestMapping("/api/public/advertisements")
class PublicAdvertisementController(private val service: AdvertisementService) { @GetMapping fun slots() = service.publicSlots() }

data class SaveAdvertisementSlotRequest(val code: String, val name: String, val description: String = "", val sortOrder: Int = 0, val enabled: Boolean = true, val system: Boolean = false) { fun draft() = AdvertisementSlotDraft(code, name, description, sortOrder, enabled, system) }
data class SaveAdvertisementRequest(val title: String, val imagePath: String, val url: String? = null, val openMode: String = "DEFAULT", val startAt: LocalDateTime? = null, val endAt: LocalDateTime? = null, val sortOrder: Int = 0, val enabled: Boolean = true) { fun draft() = AdvertisementDraft(title, imagePath, url, openMode, startAt, endAt, sortOrder, enabled) }
