package com.jilinjobs.cms.staticresource

import com.jilinjobs.cms.siteconfig.SiteConfigMapper
import jakarta.servlet.http.HttpServletRequest
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.time.Instant
import java.util.UUID
import java.util.zip.ZipInputStream
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

data class StaticEntry(
 val path:String,
 val name:String,
 val directory:Boolean,
 val size:Long,
 val modifiedAt:Instant?,
 val protectedResource:Boolean=false,
)
data class TrashEntry(val id:String,val originalPath:String)
class StaticResourceValidationException(message:String):RuntimeException(message)
class StaticResourceNotFoundException(path:String):RuntimeException("静态资源不存在：$path")

@Service
class StaticResourceService(
 @Value("\${cms.static.root:./data/static}") rootText:String,
 private val siteConfigMapper:SiteConfigMapper,
){
 private val root=Paths.get(rootText).toAbsolutePath().normalize().also{Files.createDirectories(it)}
 private val trashRoot=root.resolve(".trash").also{Files.createDirectories(it)}
 private val allowed=setOf("png","jpg","jpeg","gif","webp","ico","pdf","doc","docx","xls","xlsx")

 fun list(path:String=""):List<StaticEntry>{
  val dir=safeDirectory(path)
  if(!Files.exists(dir))throw StaticResourceNotFoundException(path)
  if(!Files.isDirectory(dir))throw StaticResourceValidationException("指定路径不是目录")
  val protectedPaths=protectedPaths()
  return Files.list(dir).use{stream->
   stream.filter{!it.startsWith(trashRoot)}
    .map{entry(it,protectedPaths)}
    .sorted(compareBy<StaticEntry>{!it.directory}.thenBy{it.name})
    .toList()
  }
 }

 fun upload(path:String,file:MultipartFile,replace:Boolean):StaticEntry{
  if(file.isEmpty)throw StaticResourceValidationException("上传文件不能为空")
  val relative=normalizeFile(path)
  val ext=checkExtension(relative)
  validateContent(ext,file)
  val target=safeFile(relative)
  Files.createDirectories(target.parent)
  if(Files.exists(target)&&!replace)throw StaticResourceValidationException("目标文件已存在，请明确选择替换")
  file.inputStream.use{Files.copy(it,target,StandardCopyOption.REPLACE_EXISTING)}
  return entry(target,protectedPaths())
 }

 fun delete(path:String):TrashEntry{
  val relative=normalizeFile(path)
  if(relative in protectedPaths()) throw StaticResourceValidationException("该资源属于站点关键资源，不能通过普通删除入口移除；如需更新请使用明确替换操作")
  val source=safeFile(relative)
  if(!Files.isRegularFile(source))throw StaticResourceNotFoundException(path)
  val id=UUID.randomUUID().toString()
  Files.move(source,trashRoot.resolve("$id.data"),StandardCopyOption.REPLACE_EXISTING)
  Files.writeString(trashRoot.resolve("$id.meta"),relative)
  return TrashEntry(id,relative)
 }

 fun trash(): List<TrashEntry> = Files.list(trashRoot).use { stream ->
  stream.filter { p -> p.fileName.toString().endsWith(".meta") }
   .map { p -> TrashEntry(p.fileName.toString().removeSuffix(".meta"), Files.readString(p)) }
   .toList()
 }

 fun restore(id:String):StaticEntry{
  if(!id.matches(Regex("[0-9a-f-]{36}")))throw StaticResourceValidationException("回收记录无效")
  val data=trashRoot.resolve("$id.data")
  val meta=trashRoot.resolve("$id.meta")
  if(!Files.isRegularFile(data)||!Files.isRegularFile(meta))throw StaticResourceNotFoundException(id)
  val target=safeFile(Files.readString(meta))
  if(Files.exists(target))throw StaticResourceValidationException("原路径已有文件，不能直接恢复")
  Files.createDirectories(target.parent)
  Files.move(data,target)
  Files.deleteIfExists(meta)
  return entry(target,protectedPaths())
 }

 fun resolvePublic(path:String):Path{
  val relative=normalizeFile(path)
  val p=safeFile(relative)
  if(!Files.isRegularFile(p))throw StaticResourceNotFoundException(path)
  return p
 }

 private fun checkExtension(path:String):String{
  val ext=path.substringAfterLast('.',"").lowercase()
  if(ext !in allowed)throw StaticResourceValidationException("该文件类型不允许通过网站静态资源管理上传")
  return ext
 }

 private fun validateContent(ext:String,file:MultipartFile){
  val header=file.inputStream.use{it.readNBytes(16)}
  val matches=when(ext){
   "png" -> header.startsWith(byteArrayOf(0x89.toByte(),0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a))
   "jpg","jpeg" -> header.size>=2 && header[0]==0xff.toByte() && header[1]==0xd8.toByte()
   "gif" -> header.asAscii(6) in setOf("GIF87a","GIF89a")
   "webp" -> header.asAscii(4)=="RIFF" && header.drop(8).take(4).toByteArray().toString(StandardCharsets.US_ASCII)=="WEBP"
   "ico" -> header.startsWith(byteArrayOf(0x00,0x00,0x01,0x00))
   "pdf" -> header.asAscii(5)=="%PDF-"
   "doc","xls" -> header.startsWith(byteArrayOf(0xd0.toByte(),0xcf.toByte(),0x11,0xe0.toByte(),0xa1.toByte(),0xb1.toByte(),0x1a,0xe1.toByte()))
   "docx" -> isOfficeOpenXml(file,"word/")
   "xlsx" -> isOfficeOpenXml(file,"xl/")
   else -> false
  }
  if(!matches)throw StaticResourceValidationException("文件实际内容与 .$ext 扩展名不匹配")
 }

 private fun isOfficeOpenXml(file:MultipartFile,requiredPrefix:String):Boolean=runCatching{
  ZipInputStream(file.inputStream).use{zip->
   var entry=zip.nextEntry
   while(entry!=null){
    if(entry.name.startsWith(requiredPrefix)) return@use true
    entry=zip.nextEntry
   }
   false
  }
 }.getOrDefault(false)

 private fun ByteArray.startsWith(prefix:ByteArray):Boolean=size>=prefix.size && prefix.indices.all{this[it]==prefix[it]}
 private fun ByteArray.asAscii(length:Int):String=take(length).toByteArray().toString(StandardCharsets.US_ASCII)

 private fun protectedPaths():Set<String>=buildSet{
  add("health/baseline.png")
  siteConfigMapper.findAll().forEach{row->
   if(row.valueType=="RESOURCE_PATH") normalizedConfiguredStaticPath(row.configValue)?.let(::add)
   if(row.configKey=="HOME_BANNERS"){
    Regex("\\\"image\\\"\\s*:\\s*\\\"/static/([^\\\"]+)\\\"").findAll(row.configValue)
     .map{it.groupValues[1]}
     .forEach(::add)
   }
  }
 }

 private fun normalizedConfiguredStaticPath(value:String):String?{
  val trimmed=value.trim()
  if(!trimmed.startsWith("/static/"))return null
  return runCatching{normalizeFile(trimmed.removePrefix("/static/"))}.getOrNull()
 }

 private fun normalizeFile(raw:String):String{
  val v=raw.trim().replace('\\','/').trimStart('/')
  if(v.isBlank()||v.split('/').any{it==".."||it=="."||it.isBlank()})throw StaticResourceValidationException("静态资源路径不合法")
  if(v.startsWith(".trash"))throw StaticResourceValidationException("不能操作回收区内部路径")
  return v
 }

 private fun safeFile(relative:String):Path{
  val p=root.resolve(relative).normalize()
  if(!p.startsWith(root)||p.startsWith(trashRoot))throw StaticResourceValidationException("静态资源路径越界")
  return p
 }

 private fun safeDirectory(raw:String):Path{
  if(raw.isBlank())return root
  val v=raw.trim().replace('\\','/').trim('/')
  if(v.split('/').any{it==".."||it=="."||it.isBlank()})throw StaticResourceValidationException("静态资源路径不合法")
  val p=root.resolve(v).normalize()
  if(!p.startsWith(root)||p.startsWith(trashRoot))throw StaticResourceValidationException("静态资源路径越界")
  return p
 }

 private fun entry(p:Path,protectedPaths:Set<String>)=StaticEntry(
  path=root.relativize(p).toString().replace('\\','/'),
  name=p.fileName.toString(),
  directory=Files.isDirectory(p),
  size=if(Files.isRegularFile(p))Files.size(p) else 0,
  modifiedAt=runCatching{Files.getLastModifiedTime(p).toInstant()}.getOrNull(),
  protectedResource=Files.isRegularFile(p) && root.relativize(p).toString().replace('\\','/') in protectedPaths,
 )
}

@RestController
@RequestMapping("/api/admin/static-resources")
class AdminStaticResourceController(private val service:StaticResourceService){
 @GetMapping fun list(@RequestParam(defaultValue="") path:String)=service.list(path)
 @PostMapping(consumes=[MediaType.MULTIPART_FORM_DATA_VALUE]) fun upload(@RequestParam path:String,@RequestPart file:MultipartFile,@RequestParam(defaultValue="false") replace:Boolean)=service.upload(path,file,replace)
 @DeleteMapping fun delete(@RequestParam path:String)=service.delete(path)
 @GetMapping("/trash") fun trash()=service.trash()
 @PostMapping("/restore/{id}") fun restore(@PathVariable id:String)=service.restore(id)
}

@RestController
class PublicStaticResourceController(private val service:StaticResourceService){
 @GetMapping("/static/**")
 fun get(request:HttpServletRequest):ResponseEntity<FileSystemResource>{
  val path=request.requestURI.substringAfter("/static/")
  val file=service.resolvePublic(path)
  val media=runCatching{MediaType.parseMediaType(Files.probeContentType(file)?:"application/octet-stream")}.getOrDefault(MediaType.APPLICATION_OCTET_STREAM)
  return ResponseEntity.ok().contentType(media).body(FileSystemResource(file))
 }
}
