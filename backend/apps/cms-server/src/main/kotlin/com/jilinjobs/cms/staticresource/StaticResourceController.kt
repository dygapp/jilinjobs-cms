package com.jilinjobs.cms.staticresource

import jakarta.servlet.http.HttpServletRequest
import java.nio.file.Files
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/static-resources")
class AdminStaticResourceController(private val service: StaticResourceService) {
    @GetMapping fun list(@RequestParam(defaultValue = "") path: String) = service.list(path)
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]) fun upload(@RequestParam path: String, @RequestPart file: MultipartFile, @RequestParam(defaultValue = "false") replace: Boolean) = service.upload(path, file, replace)
    @DeleteMapping fun delete(@RequestParam path: String) = service.delete(path)
    @GetMapping("/trash") fun trash() = service.trash()
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
