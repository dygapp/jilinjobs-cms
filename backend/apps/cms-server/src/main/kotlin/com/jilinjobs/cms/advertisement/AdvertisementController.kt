package com.jilinjobs.cms.advertisement

import java.time.LocalDateTime
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
class PublicAdvertisementController(private val service: AdvertisementService) {
    @GetMapping fun slots() = service.publicSlots()
}

data class SaveAdvertisementSlotRequest(val code: String, val name: String, val description: String = "", val sortOrder: Int = 0, val enabled: Boolean = true, val system: Boolean = false) {
    fun draft() = AdvertisementSlotDraft(code, name, description, sortOrder, enabled, system)
}

data class SaveAdvertisementRequest(val title: String, val imagePath: String, val url: String? = null, val openMode: String = "DEFAULT", val startAt: LocalDateTime? = null, val endAt: LocalDateTime? = null, val sortOrder: Int = 0, val enabled: Boolean = true) {
    fun draft() = AdvertisementDraft(title, imagePath, url, openMode, startAt, endAt, sortOrder, enabled)
}
