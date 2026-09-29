package com.jilinjobs.cms.staticresource

import com.jilinjobs.cms.audit.*
import com.jilinjobs.cms.security.CmsAdminAccess

import com.jilinjobs.cms.resource.toUploadContent
import jakarta.servlet.http.HttpServletRequest
import java.nio.file.Files
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@CmsAdminAccess
@RestController
@RequestMapping("/api/admin/static-resources")
class AdminStaticResourceController(private val service: StaticResourceService) {
    @GetMapping fun list(@RequestParam(defaultValue = "") path: String) = service.list(path)
    @AdminAuditOperation(
        action = AdminAuditAction.UPLOAD,
        objectType = AdminAuditObjectType.STATIC_RESOURCE,
        returnProperty = "path",
        fallbackObjectIdParameter = "path",
        alternateAction = AdminAuditAction.REPLACE,
        alternateActionParameter = "replace",
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]) fun upload(@RequestParam path: String, @RequestPart file: MultipartFile, @RequestParam(defaultValue = "false") replace: Boolean) = service.upload(path, file.toUploadContent(), replace)
    @AdminAuditOperation(AdminAuditAction.TRASH, AdminAuditObjectType.STATIC_RESOURCE, AdminAuditObjectIdSource.ARGUMENT, "path")
    @DeleteMapping fun delete(@RequestParam path: String) = service.delete(path)
    @GetMapping("/trash") fun trash() = service.trash()
    @AdminAuditOperation(
        AdminAuditAction.RESTORE,
        AdminAuditObjectType.STATIC_RESOURCE,
        returnProperty = "path",
        fallbackObjectIdParameter = "id",
    )
    @PostMapping("/restore/{id}") fun restore(@PathVariable id: String) = service.restore(id)
}

@RestController
class PublicStaticResourceController(private val service: StaticResourceService) {
    @GetMapping("/static/**")
    fun get(request: HttpServletRequest): ResponseEntity<FileSystemResource> {
        val path = request.requestURI.substringAfter("/static/")
        val file = service.resolvePublic(path)
        val media = runCatching { MediaType.parseMediaType(Files.probeContentType(file) ?: "application/octet-stream") }.getOrDefault(MediaType.APPLICATION_OCTET_STREAM)
        return ResponseEntity.ok().contentType(media).body(FileSystemResource(file))
    }
}
