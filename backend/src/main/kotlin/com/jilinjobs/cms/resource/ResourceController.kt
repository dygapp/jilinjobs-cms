package com.jilinjobs.cms.resource

import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/resources")
class ResourceController(
    private val service: ResourceService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<CmsResource> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.upload(file))

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): CmsResource = service.get(id)

    @GetMapping("/{id}/content")
    fun content(@PathVariable id: Long): ResponseEntity<Resource> {
        val (metadata, path) = service.resolveContent(id)
        val mediaType = metadata.contentType
            ?.let { runCatching { MediaType.parseMediaType(it) }.getOrNull() }
            ?: MediaType.APPLICATION_OCTET_STREAM
        val body: Resource = FileSystemResource(path)
        return ResponseEntity.ok()
            .contentType(mediaType)
            .contentLength(metadata.sizeBytes)
            .body(body)
    }
}
