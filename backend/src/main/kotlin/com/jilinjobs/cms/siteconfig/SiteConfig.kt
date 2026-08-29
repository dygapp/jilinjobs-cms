package com.jilinjobs.cms.siteconfig

import java.net.URI
import org.apache.ibatis.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.ObjectMapper

data class SiteConfigItem(val key:String,val name:String,val groupCode:String,val value:String,val valueType:String,val description:String,val sortOrder:Int,val required:Boolean,val system:Boolean,val enabled:Boolean)
data class SiteConfigDraft(val key:String,val name:String,val groupCode:String="GENERAL",val value:String="",val valueType:String="TEXT",val description:String="",val sortOrder:Int=0,val required:Boolean=false,val system:Boolean=false,val enabled:Boolean=true)
class SiteConfigValidationException(message:String):RuntimeException(message)
class SiteConfigNotFoundException(key:String):RuntimeException("网站属性不存在：$key")
data class SiteConfigRecord(var configKey:String="",var propertyName:String="",var groupCode:String="GENERAL",var configValue:String="",var valueType:String="TEXT",var description:String="",var sortOrder:Int=0,var required:Boolean=false,var systemFlag:Boolean=false,var enabled:Boolean=true)

@Mapper
interface SiteConfigMapper{
 @Select("SELECT config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system_flag,enabled FROM cms_site_config ORDER BY group_code,sort_order,config_key") fun findAll():List<SiteConfigRecord>
 @Select("SELECT config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system_flag,enabled FROM cms_site_config WHERE enabled=1 ORDER BY group_code,sort_order,config_key") fun findEnabled():List<SiteConfigRecord>
 @Select("SELECT config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system_flag,enabled FROM cms_site_config WHERE config_key=#{key}") fun find(@Param("key") key:String):SiteConfigRecord?
 @Insert("INSERT INTO cms_site_config(config_key,property_name,group_code,config_value,value_type,description,sort_order,required,system_flag,enabled) VALUES(#{configKey},#{propertyName},#{groupCode},#{configValue},#{valueType},#{description},#{sortOrder},#{required},#{systemFlag},#{enabled})") fun insert(record:SiteConfigRecord):Int
 @Update("UPDATE cms_site_config SET property_name=#{propertyName},group_code=#{groupCode},config_value=#{configValue},value_type=#{valueType},description=#{description},sort_order=#{sortOrder},required=#{required},system_flag=#{systemFlag},enabled=#{enabled} WHERE config_key=#{configKey}") fun updateDefinition(record:SiteConfigRecord):Int
 @Update("UPDATE cms_site_config SET config_value=#{value} WHERE config_key=#{key}") fun update(@Param("key") key:String,@Param("value") value:String):Int
 @Delete("DELETE FROM cms_site_config WHERE config_key=#{key}") fun delete(@Param("key") key:String):Int
}

@Service
class SiteConfigService(private val mapper:SiteConfigMapper,private val objectMapper:ObjectMapper){
 private val allowedTypes=setOf("TEXT","RESOURCE_PATH","JSON","URL","BOOLEAN")
 @Transactional(readOnly=true) fun list()=mapper.findAll().map{it.item()}
 @Transactional(readOnly=true) fun listPublic()=mapper.findEnabled().map{it.item()}
 @Transactional fun create(d:SiteConfigDraft):SiteConfigItem{val n=normalize(d);if(mapper.find(n.key)!=null)throw SiteConfigValidationException("网站属性 Key 已存在：${n.key}");val r=n.record();mapper.insert(r);return r.item()}
 @Transactional fun updateDefinition(key:String,d:SiteConfigDraft):SiteConfigItem{val k=normalizeKey(key);mapper.find(k)?:throw SiteConfigNotFoundException(k);mapper.updateDefinition(normalize(d.copy(key=k)).record());return mapper.find(k)!!.item()}
 @Transactional fun update(key:String,value:String):SiteConfigItem{val k=normalizeKey(key);val r=mapper.find(k)?:throw SiteConfigNotFoundException(k);validateValue(r.valueType,value,r.required,k);mapper.update(k,value);return mapper.find(k)!!.item()}
 @Transactional fun delete(key:String){val k=normalizeKey(key);mapper.find(k)?:throw SiteConfigNotFoundException(k);mapper.delete(k)}
 private fun normalize(d:SiteConfigDraft):SiteConfigDraft{val key=normalizeKey(d.key);val name=d.name.trim();if(name.isBlank())throw SiteConfigValidationException("网站属性名称不能为空");if(name.length>100)throw SiteConfigValidationException("网站属性名称不能超过 100 个字符");val group=d.groupCode.trim().uppercase().ifBlank{"GENERAL"};if(!group.matches(Regex("[A-Z][A-Z0-9_]{1,49}")))throw SiteConfigValidationException("属性分组 Code 格式不正确");val type=d.valueType.trim().uppercase();if(type !in allowedTypes)throw SiteConfigValidationException("不支持的网站属性类型：$type");validateValue(type,d.value,d.required,key);return d.copy(key=key,name=name,groupCode=group,valueType=type,description=d.description.trim())}
 private fun normalizeKey(raw:String):String{val k=raw.trim().uppercase();if(!k.matches(Regex("[A-Z][A-Z0-9_]{1,99}")))throw SiteConfigValidationException("网站属性 Key 必须由大写字母、数字和下划线组成");return k}
 private fun validateValue(type:String,value:String,required:Boolean,key:String){val v=value.trim();if(required&&v.isBlank())throw SiteConfigValidationException("网站属性 $key 不能为空");if(v.isBlank())return;when(type){"JSON"->{val n=runCatching{objectMapper.readTree(value)}.getOrElse{throw SiteConfigValidationException("网站属性 $key 必须是合法 JSON")};if(n==null||(!n.isArray&&!n.isObject))throw SiteConfigValidationException("网站属性 $key 必须是 JSON 数组或对象")};"RESOURCE_PATH"->if(!v.startsWith("/static/"))throw SiteConfigValidationException("网站属性 $key 必须使用 /static/ 资源路径");"URL"->validateUrl(v,key);"BOOLEAN"->if(v.lowercase() !in setOf("true","false"))throw SiteConfigValidationException("网站属性 $key 必须是 true 或 false")}}
 private fun validateUrl(v:String,key:String){if(v.startsWith("/")&&!v.startsWith("//"))return;val u=runCatching{URI(v)}.getOrElse{throw SiteConfigValidationException("网站属性 $key URL 格式不正确")};if(u.scheme?.lowercase() !in setOf("http","https")||u.host.isNullOrBlank())throw SiteConfigValidationException("网站属性 $key 必须是站内路径或 HTTP(S) 地址")}
 private fun SiteConfigDraft.record()=SiteConfigRecord(key,name,groupCode,value,valueType,description,sortOrder,required,system,enabled)
 private fun SiteConfigRecord.item()=SiteConfigItem(configKey,propertyName,groupCode,configValue,valueType,description,sortOrder,required,systemFlag,enabled)
}

@RestController @RequestMapping("/api/admin/site-config")
class AdminSiteConfigController(private val service:SiteConfigService){
 @GetMapping fun list()=service.list();@PostMapping fun create(@RequestBody r:SaveSiteConfigRequest)=ResponseEntity.status(HttpStatus.CREATED).body(service.create(r.draft()));@PutMapping("/{key}") fun updateValue(@PathVariable key:String,@RequestBody r:SiteConfigUpdateRequest)=service.update(key,r.value);@PutMapping("/{key}/definition") fun updateDefinition(@PathVariable key:String,@RequestBody r:SaveSiteConfigRequest)=service.updateDefinition(key,r.draft());@DeleteMapping("/{key}") fun delete(@PathVariable key:String):ResponseEntity<Void>{service.delete(key);return ResponseEntity.noContent().build()}
}
@RestController @RequestMapping("/api/public/site-config") class PublicSiteConfigController(private val service:SiteConfigService){@GetMapping fun list()=service.listPublic()}
data class SiteConfigUpdateRequest(val value:String)
data class SaveSiteConfigRequest(val key:String,val name:String,val groupCode:String="GENERAL",val value:String="",val valueType:String="TEXT",val description:String="",val sortOrder:Int=0,val required:Boolean=false,val system:Boolean=false,val enabled:Boolean=true){fun draft()=SiteConfigDraft(key,name,groupCode,value,valueType,description,sortOrder,required,system,enabled)}
