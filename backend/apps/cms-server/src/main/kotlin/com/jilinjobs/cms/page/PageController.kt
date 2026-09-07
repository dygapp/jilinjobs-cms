package com.jilinjobs.cms.page

import org.springframework.http.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/page-groups")
class AdminPageGroupController(private val service:PageService) {
    @GetMapping fun list()=service.listGroups()
    @PostMapping fun create(@RequestBody draft:PageGroupDraft)=ResponseEntity.status(HttpStatus.CREATED).body(service.createGroup(draft))
    @PutMapping("/{id}") fun update(@PathVariable id:Long,@RequestBody draft:PageGroupDraft)=service.updateGroup(id,draft)
}

@RestController
@RequestMapping("/api/admin/pages")
class AdminPageController(private val service:PageService) {
    @GetMapping fun list()=service.listPages()
    @PostMapping fun create(@RequestBody draft:PageDraft)=ResponseEntity.status(HttpStatus.CREATED).body(service.createPage(draft))
    @PutMapping("/{id}") fun update(@PathVariable id:Long,@RequestBody draft:PageDraft)=service.updatePage(id,draft)
    @DeleteMapping("/{id}") fun delete(@PathVariable id:Long):ResponseEntity<Void>{service.deletePage(id);return ResponseEntity.noContent().build()}
}

@RestController
@RequestMapping("/api/public")
class PublicPageController(private val service:PageService) {
    @GetMapping("/pages/{alias}") fun page(@PathVariable alias:String)=service.getPublicStandalone(alias)
    @GetMapping("/page-groups/{groupAlias}") fun group(@PathVariable groupAlias:String)=service.getPublicGroup(groupAlias)
    @GetMapping("/page-groups/{groupAlias}/{alias}") fun grouped(@PathVariable groupAlias:String,@PathVariable alias:String)=service.getPublicGrouped(groupAlias,alias)
}
