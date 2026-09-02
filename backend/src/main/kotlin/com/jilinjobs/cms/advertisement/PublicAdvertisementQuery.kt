package com.jilinjobs.cms.advertisement

import java.time.LocalDateTime
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class PublicAdvertisementQueryRow(
    var slotId: Long = 0,
    var slotCode: String = "",
    var slotName: String = "",
    var advertisementId: Long? = null,
    var title: String? = null,
    var imagePath: String? = null,
    var url: String? = null,
    var openMode: String? = null,
    var startAt: LocalDateTime? = null,
    var endAt: LocalDateTime? = null,
    var sortOrder: Int? = null,
    var enabled: Boolean? = null,
)

@Mapper
interface PublicAdvertisementQueryMapper {
    @Select(
        """
        SELECT s.id AS slot_id, s.code AS slot_code, s.name AS slot_name,
               a.id AS advertisement_id, a.title, a.image_path, a.url, a.open_mode,
               a.start_at, a.end_at, a.sort_order, a.enabled
        FROM cms_ad_slot s
        LEFT JOIN cms_advertisement a
          ON a.slot_id = s.id
         AND a.enabled = 1
         AND (a.start_at IS NULL OR a.start_at <= CURRENT_TIMESTAMP)
         AND (a.end_at IS NULL OR a.end_at > CURRENT_TIMESTAMP)
        WHERE s.enabled = 1 AND s.code = #{code}
        ORDER BY s.sort_order, s.id, a.sort_order, a.id
        """,
    )
    fun findByCode(@Param("code") code: String): List<PublicAdvertisementQueryRow>
}

@Service
class PublicAdvertisementQueryService(private val mapper: PublicAdvertisementQueryMapper) {
    @Transactional(readOnly = true)
    fun byCode(rawCode: String): PublicAdvertisementSlot {
        val code = rawCode.trim().uppercase()
        val rows = mapper.findByCode(code)
        if (rows.isEmpty()) throw AdvertisementSlotNotFoundException(code)
        val slot = rows.first()
        return PublicAdvertisementSlot(
            id = slot.slotId,
            code = slot.slotCode,
            name = slot.slotName,
            advertisements = rows.mapNotNull { row ->
                row.advertisementId?.let { id ->
                    Advertisement(
                        id = id,
                        slotId = row.slotId,
                        title = row.title.orEmpty(),
                        imagePath = row.imagePath.orEmpty(),
                        url = row.url,
                        openMode = row.openMode ?: "DEFAULT",
                        startAt = row.startAt,
                        endAt = row.endAt,
                        sortOrder = row.sortOrder ?: 0,
                        enabled = row.enabled ?: true,
                    )
                }
            },
        )
    }
}

@RestController
@RequestMapping("/api/public/advertisements")
class PublicAdvertisementQueryController(private val service: PublicAdvertisementQueryService) {
    @GetMapping("/slots/{code}") fun byCode(@PathVariable code: String) = service.byCode(code)
}
