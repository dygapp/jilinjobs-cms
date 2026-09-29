package com.jilinjobs.cms.page

import com.jilinjobs.cms.audit.*
import com.jilinjobs.cms.security.CmsAdminAccess

import org.springframework.http.*
import org.springframework.web.bind.annotation.*

@CmsAdminAccess
@RestController
@RequestMapping("/api/admin/page-groups")
class AdminPageGroupController(private val service:PageService) {
    @GetMapping fun list()=service.listGroups()
    @AdminAuditOperation(AdminAuditAction.CREATE, AdminAuditObjectType.PAGE_GROUP)
    @PostMapping fun create(@RequestBody draft:PageGroupDraft)=ResponseEntity.status(HttpStatus.CREATED).body(service.createGroup(draft))
    @AdminAuditOperation(AdminAuditAction.UPDATE, AdminAuditObjectType.PAGE_GROUP, AdminAuditObjectIdSource.ARGUMENT, "id")
    @PutMapping("/{id}") fun update(@PathVariable id:Long,@RequestBody draft:PageGroupDraft)=service.updateGroup(id,draft)
}

@CmsAdminAccess
@RestController
@RequestMapping("/api/admin/pages")
class AdminPageController(private val service:PageService) {
    @GetMapping fun list()=service.listPages()
    @AdminAuditOperation(AdminAuditAction.CREATE, AdminAuditObjectType.PAGE)
    @PostMapping fun create(@RequestBody draft:PageDraft)=ResponseEntity.status(HttpStatus.CREATED).body(service.createPage(draft))
    @AdminAuditOperation(AdminAuditAction.UPDATE, AdminAuditObjectType.PAGE, AdminAuditObjectIdSource.ARGUMENT, "id")
    @PutMapping("/{id}") fun update(@PathVariable id:Long,@RequestBody draft:PageDraft)=service.updatePage(id,draft)
    @AdminAuditOperation(AdminAuditAction.DELETE, AdminAuditObjectType.PAGE, AdminAuditObjectIdSource.ARGUMENT, "id")
    @DeleteMapping("/{id}") fun delete(@PathVariable id:Long):ResponseEntity<Void>{service.deletePage(id);return ResponseEntity.noContent().build()}
}

@RestController
@RequestMapping("/api/public")
class PublicPageController(private val service:PageService) {
    @GetMapping("/pages/{alias}") fun page(@PathVariable alias:String)=service.getPublicStandalone(alias)
    @GetMapping("/page-groups/{groupAlias}") fun group(@PathVariable groupAlias:String)=service.getPublicGroup(groupAlias)
    @GetMapping("/page-groups/{groupAlias}/{alias}") fun grouped(@PathVariable groupAlias:String,@PathVariable alias:String)=service.getPublicGrouped(groupAlias,alias)
}
