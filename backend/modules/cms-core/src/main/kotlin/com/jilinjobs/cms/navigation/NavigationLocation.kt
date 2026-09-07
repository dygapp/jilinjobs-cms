package com.jilinjobs.cms.navigation

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.apache.ibatis.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

data class NavigationLocation(val id:Long,val code:String,val name:String,val description:String,val sortOrder:Int,val enabled:Boolean,val system:Boolean,val preset:Boolean=false)
data class NavigationLocationDraft(val code:String,val name:String,val description:String="",val sortOrder:Int=0,val enabled:Boolean=true,val system:Boolean=false)
data class NavigationLocationRecord(var id:Long?=null,var code:String="",var name:String="",var description:String="",var sortOrder:Int=0,var enabled:Boolean=true,var systemFlag:Boolean=false,var preset:Boolean=false)
class NavigationLocationNotFoundException(code:String):RuntimeException("导航位置不存在：$code")

@Mapper
interface NavigationLocationMapper{
 @Select("SELECT id,code,name,description,sort_order,enabled,system_flag,preset FROM cms_navigation_location ORDER BY sort_order,id") fun findAll():List<NavigationLocationRecord>
 @Select("SELECT id,code,name,description,sort_order,enabled,system_flag,preset FROM cms_navigation_location WHERE code=#{code}") fun findByCode(@Param("code") code:String):NavigationLocationRecord?
 @Insert("INSERT INTO cms_navigation_location(code,name,description,sort_order,enabled,system_flag) VALUES(#{code},#{name},#{description},#{sortOrder},#{enabled},#{systemFlag})") @Options(useGeneratedKeys=true,keyProperty="id") fun insert(record:NavigationLocationRecord):Int
 @Update("UPDATE cms_navigation_location SET name=#{name},description=#{description},sort_order=#{sortOrder},enabled=#{enabled},system_flag=#{systemFlag} WHERE code=#{code}") fun update(record:NavigationLocationRecord):Int
 @Delete("DELETE FROM cms_navigation_location WHERE code=#{code}") fun delete(@Param("code") code:String):Int
}

@Service
class NavigationLocationService(private val mapper:NavigationLocationMapper,private val navigationMapper:NavigationMapper){
 @Transactional(readOnly=true) fun list()=mapper.findAll().map{it.model()}
 @Transactional fun create(d:NavigationLocationDraft):NavigationLocation{val n=normalize(d);if(mapper.findByCode(n.code)!=null)throw NavigationValidationException("导航位置 Code 已存在：${n.code}");val r=n.record();mapper.insert(r);return mapper.findByCode(n.code)!!.model()}
 @Transactional fun update(code:String,d:NavigationLocationDraft):NavigationLocation{val c=normalizeCode(code);mapper.findByCode(c)?:throw NavigationLocationNotFoundException(c);mapper.update(normalize(d.copy(code=c)).record());return mapper.findByCode(c)!!.model()}
 @Transactional fun delete(code:String){val c=normalizeCode(code);val current=mapper.findByCode(c)?:throw NavigationLocationNotFoundException(c);if(current.preset)throw NavigationValidationException("预置导航位置属于网站规划基线，不能删除");if(navigationMapper.countByPosition(c)>0)throw NavigationValidationException("该导航位置仍包含导航条目，不能删除");mapper.delete(c)}
 private fun normalize(d:NavigationLocationDraft):NavigationLocationDraft{val code=normalizeCode(d.code);val name=d.name.trim();if(name.isBlank())throw NavigationValidationException("导航位置名称不能为空");if(name.length>100)throw NavigationValidationException("导航位置名称不能超过 100 个字符");return d.copy(code=code,name=name,description=d.description.trim())}
 private fun normalizeCode(raw:String):String{val code=raw.trim().uppercase();if(!code.matches(Regex("[A-Z][A-Z0-9_]{1,49}")))throw NavigationValidationException("导航位置 Code 必须由大写字母、数字和下划线组成");return code}
 private fun NavigationLocationDraft.record()=NavigationLocationRecord(code=code,name=name,description=description,sortOrder=sortOrder,enabled=enabled,systemFlag=system)
 private fun NavigationLocationRecord.model()=NavigationLocation(requireNotNull(id),code,name,description,sortOrder,enabled,systemFlag,preset)
}

@RestController @RequestMapping("/api/admin/navigation-locations")
class AdminNavigationLocationController(private val service:NavigationLocationService){
 @GetMapping fun list()=service.list();@PostMapping fun create(@Valid @RequestBody r:SaveNavigationLocationRequest)=ResponseEntity.status(HttpStatus.CREATED).body(service.create(r.draft()));@PutMapping("/{code}") fun update(@PathVariable code:String,@Valid @RequestBody r:SaveNavigationLocationRequest)=service.update(code,r.draft());@DeleteMapping("/{code}") fun delete(@PathVariable code:String):ResponseEntity<Void>{service.delete(code);return ResponseEntity.noContent().build()}
}
data class SaveNavigationLocationRequest(@field:NotBlank @field:Size(max=50) val code:String,@field:NotBlank @field:Size(max=100) val name:String,@field:Size(max=255) val description:String="",val sortOrder:Int=0,val enabled:Boolean=true,val system:Boolean=false){fun draft()=NavigationLocationDraft(code,name,description,sortOrder,enabled,system)}
