package com.jilinjobs.cms.siteconfig

import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.ObjectMapper

data class SiteConfigItem(val key:String,val value:String,val valueType:String,val description:String)
class SiteConfigValidationException(message:String):RuntimeException(message)
class SiteConfigNotFoundException(key:String):RuntimeException("网站配置不存在：$key")

enum class SiteConfigKey(val type:String){
 SITE_NAME("TEXT"),
 SITE_SHORT_NAME("TEXT"),
 LOGO_PATH("RESOURCE_PATH"),
 PLATFORM_LOGO_ICON_PATH("RESOURCE_PATH"),
 PLATFORM_LOGO_TEXT_PATH("RESOURCE_PATH"),
 HEADER_BANNER_PATH("RESOURCE_PATH"),
 HOME_PROMO_BANNER_PATH("RESOURCE_PATH"),
 HOME_NCSS_LOGO_PATH("RESOURCE_PATH"),
 CONTACT_PHONE("TEXT"),
 CONTACT_ADDRESS("TEXT"),
 OFFICE_HOURS("TEXT"),
 ICP_NUMBER("TEXT"),
 FOOTER_COPYRIGHT("TEXT"),
 HOME_BANNERS("JSON"),
 SERVICE_LINKS("JSON"),
 SITE_LINK_GROUPS("JSON")
}

@Mapper
interface SiteConfigMapper {
 @Select("SELECT config_key,config_value,value_type,description FROM cms_site_config ORDER BY config_key") fun findAll():List<SiteConfigRecord>
 @Select("SELECT config_key,config_value,value_type,description FROM cms_site_config WHERE config_key=#{key}") fun find(@Param("key") key:String):SiteConfigRecord?
 @Update("UPDATE cms_site_config SET config_value=#{value} WHERE config_key=#{key}") fun update(@Param("key") key:String,@Param("value") value:String):Int
}
data class SiteConfigRecord(var configKey:String="",var configValue:String="",var valueType:String="TEXT",var description:String="")

@Service
class SiteConfigService(
 private val mapper:SiteConfigMapper,
 private val objectMapper:ObjectMapper,
){
 @Transactional(readOnly=true)
 fun list()=mapper.findAll().map{SiteConfigItem(it.configKey,it.configValue,it.valueType,it.description)}

 @Transactional
 fun update(key:String,value:String):SiteConfigItem{
  val known=runCatching{SiteConfigKey.valueOf(key)}.getOrElse{throw SiteConfigValidationException("不允许的配置项：$key")}
  val row=mapper.find(key)?:throw SiteConfigNotFoundException(key)
  if(known.type!=row.valueType) throw SiteConfigValidationException("配置项 $key 的类型定义不一致")
  validateValue(known,value)
  mapper.update(key,value)
  return SiteConfigItem(key,value,row.valueType,row.description)
 }

 private fun validateValue(key:SiteConfigKey,value:String){
  if(key.type=="JSON"){
   val node=runCatching{objectMapper.readTree(value)}.getOrElse{throw SiteConfigValidationException("配置项 ${key.name} 必须是合法 JSON")}
   if(node==null || (!node.isArray && !node.isObject)) throw SiteConfigValidationException("配置项 ${key.name} 必须是 JSON 数组或对象")
  }
  if(key.type=="RESOURCE_PATH"){
   val trimmed=value.trim()
   if(trimmed.isNotEmpty() && !trimmed.startsWith("/static/")) throw SiteConfigValidationException("配置项 ${key.name} 必须使用 /static/ 资源路径")
  }
 }
}

@RestController
@RequestMapping("/api/admin/site-config")
class AdminSiteConfigController(private val service:SiteConfigService){
 @GetMapping fun list()=service.list()
 @PutMapping("/{key}") fun update(@PathVariable key:String,@RequestBody r:SiteConfigUpdateRequest)=service.update(key,r.value)
}

@RestController
@RequestMapping("/api/public/site-config")
class PublicSiteConfigController(private val service:SiteConfigService){
 @GetMapping fun list()=service.list()
}

data class SiteConfigUpdateRequest(val value:String)
