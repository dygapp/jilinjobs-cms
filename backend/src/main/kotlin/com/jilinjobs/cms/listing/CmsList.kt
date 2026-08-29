package com.jilinjobs.cms.listing

import java.net.URI
import org.apache.ibatis.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.ObjectMapper

data class CmsListDefinition(val id:Long,val code:String,val name:String,val groupCode:String,val itemType:String,val description:String,val sortOrder:Int,val enabled:Boolean,val system:Boolean)
data class CmsListItem(val id:Long,val listId:Long,val title:String,val subtitle:String?,val url:String?,val imagePath:String?,val openMode:String,val sortOrder:Int,val enabled:Boolean,val extraJson:String?)
data class PublicCmsList(val id:Long,val code:String,val name:String,val groupCode:String,val itemType:String,val items:List<CmsListItem>)
data class CmsListDraft(val code:String,val name:String,val groupCode:String="GENERAL",val itemType:String="LINK",val description:String="",val sortOrder:Int=0,val enabled:Boolean=true,val system:Boolean=false)
data class CmsListItemDraft(val title:String,val subtitle:String?=null,val url:String?=null,val imagePath:String?=null,val openMode:String="DEFAULT",val sortOrder:Int=0,val enabled:Boolean=true,val extraJson:String?=null)
class CmsListValidationException(message:String):RuntimeException(message)
class CmsListNotFoundException(value:String):RuntimeException("通用列表不存在：$value")
class CmsListItemNotFoundException(id:Long):RuntimeException("列表项不存在：$id")
data class CmsListRecord(var id:Long?=null,var code:String="",var name:String="",var groupCode:String="GENERAL",var itemType:String="LINK",var description:String="",var sortOrder:Int=0,var enabled:Boolean=true,var systemFlag:Boolean=false)
data class CmsListItemRecord(var id:Long?=null,var listId:Long=0,var title:String="",var subtitle:String?=null,var url:String?=null,var imagePath:String?=null,var openMode:String="DEFAULT",var sortOrder:Int=0,var enabled:Boolean=true,var extraJson:String?=null)

@Mapper
interface CmsListMapper{
 @Select("SELECT id,code,name,group_code,item_type,description,sort_order,enabled,system_flag FROM cms_list ORDER BY group_code,sort_order,id") fun findAll():List<CmsListRecord>
 @Select("SELECT id,code,name,group_code,item_type,description,sort_order,enabled,system_flag FROM cms_list WHERE enabled=1 ORDER BY group_code,sort_order,id") fun findEnabled():List<CmsListRecord>
 @Select("SELECT id,code,name,group_code,item_type,description,sort_order,enabled,system_flag FROM cms_list WHERE id=#{id}") fun findById(@Param("id") id:Long):CmsListRecord?
 @Select("SELECT id,code,name,group_code,item_type,description,sort_order,enabled,system_flag FROM cms_list WHERE code=#{code}") fun findByCode(@Param("code") code:String):CmsListRecord?
 @Insert("INSERT INTO cms_list(code,name,group_code,item_type,description,sort_order,enabled,system_flag) VALUES(#{code},#{name},#{groupCode},#{itemType},#{description},#{sortOrder},#{enabled},#{systemFlag})") @Options(useGeneratedKeys=true,keyProperty="id") fun insertList(record:CmsListRecord):Int
 @Update("UPDATE cms_list SET name=#{name},group_code=#{groupCode},item_type=#{itemType},description=#{description},sort_order=#{sortOrder},enabled=#{enabled},system_flag=#{systemFlag} WHERE id=#{id}") fun updateList(record:CmsListRecord):Int
 @Delete("DELETE FROM cms_list WHERE id=#{id}") fun deleteList(@Param("id") id:Long):Int
 @Select("SELECT id,list_id,title,subtitle,url,image_path,open_mode,sort_order,enabled,extra_json FROM cms_list_item WHERE list_id=#{listId} ORDER BY sort_order,id") fun findItems(@Param("listId") listId:Long):List<CmsListItemRecord>
 @Select("SELECT id,list_id,title,subtitle,url,image_path,open_mode,sort_order,enabled,extra_json FROM cms_list_item WHERE list_id=#{listId} AND enabled=1 ORDER BY sort_order,id") fun findEnabledItems(@Param("listId") listId:Long):List<CmsListItemRecord>
 @Select("SELECT id,list_id,title,subtitle,url,image_path,open_mode,sort_order,enabled,extra_json FROM cms_list_item WHERE id=#{id}") fun findItem(@Param("id") id:Long):CmsListItemRecord?
 @Insert("INSERT INTO cms_list_item(list_id,title,subtitle,url,image_path,open_mode,sort_order,enabled,extra_json) VALUES(#{listId},#{title},#{subtitle},#{url},#{imagePath},#{openMode},#{sortOrder},#{enabled},#{extraJson})") @Options(useGeneratedKeys=true,keyProperty="id") fun insertItem(record:CmsListItemRecord):Int
 @Update("UPDATE cms_list_item SET title=#{title},subtitle=#{subtitle},url=#{url},image_path=#{imagePath},open_mode=#{openMode},sort_order=#{sortOrder},enabled=#{enabled},extra_json=#{extraJson} WHERE id=#{id}") fun updateItem(record:CmsListItemRecord):Int
 @Delete("DELETE FROM cms_list_item WHERE id=#{id}") fun deleteItem(@Param("id") id:Long):Int
 @Select("SELECT image_path FROM cms_list_item i JOIN cms_list l ON l.id=i.list_id WHERE l.enabled=1 AND i.enabled=1 AND i.image_path LIKE '/static/%'") fun findReferencedImages():List<String>
}

@Service
class CmsListService(private val mapper:CmsListMapper,private val objectMapper:ObjectMapper){
 private val itemTypes=setOf("LINK","IMAGE_LINK","TEXT");private val openModes=setOf("DEFAULT","SAME_WINDOW","NEW_WINDOW")
 @Transactional(readOnly=true) fun listDefinitions()=mapper.findAll().map{it.model()}
 @Transactional(readOnly=true) fun listItems(listId:Long):List<CmsListItem>{requireList(listId);return mapper.findItems(listId).map{it.model()}}
 @Transactional(readOnly=true) fun publicLists()=mapper.findEnabled().map{r->PublicCmsList(requireNotNull(r.id),r.code,r.name,r.groupCode,r.itemType,mapper.findEnabledItems(requireNotNull(r.id)).map{it.model()})}
 @Transactional fun createList(d:CmsListDraft):CmsListDefinition{val n=normalizeList(d);if(mapper.findByCode(n.code)!=null)throw CmsListValidationException("列表 Code 已存在：${n.code}");val r=n.record();mapper.insertList(r);return r.model()}
 @Transactional fun updateList(id:Long,d:CmsListDraft):CmsListDefinition{val c=requireList(id);val n=normalizeList(d.copy(code=c.code));mapper.updateList(n.record(id));return mapper.findById(id)!!.model()}
 @Transactional fun deleteList(id:Long){requireList(id);mapper.deleteList(id)}
 @Transactional fun createItem(listId:Long,d:CmsListItemDraft):CmsListItem{val l=requireList(listId);val r=normalizeItem(l.itemType,d).record(listId);mapper.insertItem(r);return r.model()}
 @Transactional fun updateItem(listId:Long,id:Long,d:CmsListItemDraft):CmsListItem{val l=requireList(listId);val c=mapper.findItem(id)?:throw CmsListItemNotFoundException(id);if(c.listId!=listId)throw CmsListValidationException("列表项不属于当前列表");mapper.updateItem(normalizeItem(l.itemType,d).record(listId,id));return mapper.findItem(id)!!.model()}
 @Transactional fun deleteItem(listId:Long,id:Long){requireList(listId);val c=mapper.findItem(id)?:throw CmsListItemNotFoundException(id);if(c.listId!=listId)throw CmsListValidationException("列表项不属于当前列表");mapper.deleteItem(id)}
 private fun requireList(id:Long)=mapper.findById(id)?:throw CmsListNotFoundException(id.toString())
 private fun normalizeList(d:CmsListDraft):CmsListDraft{val code=d.code.trim().uppercase();if(!code.matches(Regex("[A-Z][A-Z0-9_]{1,99}")))throw CmsListValidationException("列表 Code 格式不正确");val name=d.name.trim();if(name.isBlank())throw CmsListValidationException("列表名称不能为空");val group=d.groupCode.trim().uppercase().ifBlank{"GENERAL"};if(!group.matches(Regex("[A-Z][A-Z0-9_]{1,49}")))throw CmsListValidationException("列表分组 Code 格式不正确");val type=d.itemType.trim().uppercase();if(type !in itemTypes)throw CmsListValidationException("不支持的列表项类型：$type");return d.copy(code=code,name=name,groupCode=group,itemType=type,description=d.description.trim())}
 private fun normalizeItem(type:String,d:CmsListItemDraft):CmsListItemDraft{val title=d.title.trim();if(title.isBlank())throw CmsListValidationException("列表项标题不能为空");val mode=d.openMode.trim().uppercase();if(mode !in openModes)throw CmsListValidationException("列表项打开方式不正确");val url=d.url?.trim()?.takeIf{it.isNotBlank()};val image=d.imagePath?.trim()?.takeIf{it.isNotBlank()};if(type in setOf("LINK","IMAGE_LINK")&&url==null)throw CmsListValidationException("链接类列表项必须填写 URL");if(url!=null)validateUrl(url);if(type=="IMAGE_LINK"&&(image==null||!image.startsWith("/static/")))throw CmsListValidationException("图片链接列表项必须使用 /static/ 图片路径");val extra=d.extraJson?.trim()?.takeIf{it.isNotBlank()};if(extra!=null)runCatching{objectMapper.readTree(extra)}.getOrElse{throw CmsListValidationException("列表项扩展数据必须是合法 JSON")};return d.copy(title=title,subtitle=d.subtitle?.trim()?.takeIf{it.isNotBlank()},url=url,imagePath=image,openMode=mode,extraJson=extra)}
 private fun validateUrl(v:String){if(v.startsWith("/")&&!v.startsWith("//"))return;val u=runCatching{URI(v)}.getOrElse{throw CmsListValidationException("列表项 URL 格式不正确")};if(u.scheme?.lowercase() !in setOf("http","https")||u.host.isNullOrBlank())throw CmsListValidationException("列表项 URL 必须是站内路径或 HTTP(S) 地址")}
 private fun CmsListDraft.record(id:Long?=null)=CmsListRecord(id,code,name,groupCode,itemType,description,sortOrder,enabled,system)
 private fun CmsListItemDraft.record(listId:Long,id:Long?=null)=CmsListItemRecord(id,listId,title,subtitle,url,imagePath,openMode,sortOrder,enabled,extraJson)
 private fun CmsListRecord.model()=CmsListDefinition(requireNotNull(id),code,name,groupCode,itemType,description,sortOrder,enabled,systemFlag)
 private fun CmsListItemRecord.model()=CmsListItem(requireNotNull(id),listId,title,subtitle,url,imagePath,openMode,sortOrder,enabled,extraJson)
}

@RestController @RequestMapping("/api/admin/lists")
class AdminCmsListController(private val service:CmsListService){@GetMapping fun lists()=service.listDefinitions();@PostMapping fun createList(@RequestBody r:SaveCmsListRequest)=ResponseEntity.status(HttpStatus.CREATED).body(service.createList(r.draft()));@PutMapping("/{id}") fun updateList(@PathVariable id:Long,@RequestBody r:SaveCmsListRequest)=service.updateList(id,r.draft());@DeleteMapping("/{id}") fun deleteList(@PathVariable id:Long):ResponseEntity<Void>{service.deleteList(id);return ResponseEntity.noContent().build()};@GetMapping("/{id}/items") fun items(@PathVariable id:Long)=service.listItems(id);@PostMapping("/{id}/items") fun createItem(@PathVariable id:Long,@RequestBody r:SaveCmsListItemRequest)=ResponseEntity.status(HttpStatus.CREATED).body(service.createItem(id,r.draft()));@PutMapping("/{listId}/items/{itemId}") fun updateItem(@PathVariable listId:Long,@PathVariable itemId:Long,@RequestBody r:SaveCmsListItemRequest)=service.updateItem(listId,itemId,r.draft());@DeleteMapping("/{listId}/items/{itemId}") fun deleteItem(@PathVariable listId:Long,@PathVariable itemId:Long):ResponseEntity<Void>{service.deleteItem(listId,itemId);return ResponseEntity.noContent().build()}}
@RestController @RequestMapping("/api/public/lists") class PublicCmsListController(private val service:CmsListService){@GetMapping fun lists()=service.publicLists()}
data class SaveCmsListRequest(val code:String,val name:String,val groupCode:String="GENERAL",val itemType:String="LINK",val description:String="",val sortOrder:Int=0,val enabled:Boolean=true,val system:Boolean=false){fun draft()=CmsListDraft(code,name,groupCode,itemType,description,sortOrder,enabled,system)}
data class SaveCmsListItemRequest(val title:String,val subtitle:String?=null,val url:String?=null,val imagePath:String?=null,val openMode:String="DEFAULT",val sortOrder:Int=0,val enabled:Boolean=true,val extraJson:String?=null){fun draft()=CmsListItemDraft(title,subtitle,url,imagePath,openMode,sortOrder,enabled,extraJson)}
