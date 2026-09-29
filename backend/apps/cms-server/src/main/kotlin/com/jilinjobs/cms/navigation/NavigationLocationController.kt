package com.jilinjobs.cms.navigation

import com.jilinjobs.cms.security.CmsAdminAccess

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CmsAdminAccess
@RestController
@RequestMapping("/api/admin/navigation-locations")
class AdminNavigationLocationController(private val service: NavigationLocationService) {
    @GetMapping
    fun list() = service.list()

    @PostMapping
    fun create(@Valid @RequestBody request: SaveNavigationLocationRequest) =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.draft()))

    @PutMapping("/{code}")
    fun update(@PathVariable code: String, @Valid @RequestBody request: SaveNavigationLocationRequest) =
        service.update(code, request.draft())

    @DeleteMapping("/{code}")
    fun delete(@PathVariable code: String): ResponseEntity<Void> {
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
