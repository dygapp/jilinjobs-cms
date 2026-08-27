package com.jilinjobs.cms.staticresource

import jakarta.servlet.http.HttpServletRequest
import java.nio.file.*
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

data class StaticEntry(val path:String,val name:String,val directory:Boolean,val size:Long,val modifiedAt:Instant?)
data class TrashEntry(val id:String,val originalPath:String)
class StaticResourceValidationException(message:String):RuntimeException(message)
class StaticResourceNotFoundException(path:String):RuntimeException("静态资源不存在：$path")

@Service
class StaticResourceService(@Value("\${cms.static.root:./data/static}") rootText:String){
 private val root=Paths.get(rootText).toAbsolutePath().normalize().also{Files.createDirectories(it)}
 private val trash=root.resolve(".trash").also{Files.createDirectories(it)}
 private val allowed=setOf("png","jpg","jpeg","gif","webp","ico","pdf","doc","docx","xls","xlsx")
 fun list(path:String=""):List<StaticEntry>{val dir=safe(path);if(!Files.exists(dir))throw StaticResourceNotFoundException(path);if(!Files.isDirectory(dir))throw StaticResourceValidationException("指定路径不是目录");return Files.list(dir).use{stream->stream.filter{!it.startsWith(trash)}.map{entry(it)}.sorted(compareBy<StaticEntry>{!it.directory}.thenBy{it.name}).toList()}}
 fun upload(path:String,file:MultipartFile,replace:Boolean):StaticEntry{if(file.isEmpty)throw StaticResourceValidationException("上传文件不能为空");val relative=normalize(path);checkExtension(relative);val target=safe(relative);Files.createDirectories(target.parent);if(Files.exists(target)&&!replace)throw StaticResourceValidationException("目标文件已存在，请明确选择替换");file.inputStream.use{Files.copy(it,target,StandardCopyOption.REPLACE_EXISTING)};return entry(target)}
 fun delete(path:String):TrashEntry{val relative=normalize(path);val source=safe(relative);if(!Files.isRegularFile(source))throw StaticResourceNotFoundException(path);val id=UUID.randomUUID().toString();Files.move(source,trash.resolve("$id.data"),StandardCopyOption.REPLACE_EXISTING);Files.writeString(trash.resolve("$id.meta"),relative);return TrashEntry(id,relative)}
 fun trash():List<TrashEntry>=Files.list(trash).use{it.filter{p->p.fileName.toString().endsWith(".meta")}.map{p->TrashEntry(p.fileName.toString().removeSuffix(".meta"),Files.readString(p))}.toList()}
 fun restore(id:String):StaticEntry{if(!id.matches(Regex("[0-9a-f-]{36}")))throw StaticResourceValidationException("回收记录无效");val data=trash.resolve("$id.data");val meta=trash.resolve("$id.meta");if(!Files.isRegularFile(data)||!Files.isRegularFile(meta))throw StaticResourceNotFoundException(id);val target=safe(Files.readString(meta));if(Files.exists(target))throw StaticResourceValidationException("原路径已有文件，不能直接恢复");Files.createDirectories(target.parent);Files.move(data,target);Files.deleteIfExists(meta);return entry(target)}
 fun resolvePublic(path:String):Path{val relative=normalize(path);if(relative.startsWith(".trash"))throw StaticResourceNotFoundException(path);val p=safe(relative);if(!Files.isRegularFile(p))throw StaticResourceNotFoundException(path);return p}
 private fun checkExtension(path:String){val ext=path.substringAfterLast('.',"").lowercase();if(ext !in allowed)throw StaticResourceValidationException("该文件类型不允许通过网站静态资源管理上传")}
 private fun normalize(raw:String):String{val v=raw.trim().replace('\\','/').trimStart('/');if(v.isBlank()||v.split('/').any{it==".."||it=="."||it.isBlank()})throw StaticResourceValidationException("静态资源路径不合法");if(v.startsWith(".trash"))throw StaticResourceValidationException("不能操作回收区内部路径");return v}
 private fun safe(relative:String):Path{val p=root.resolve(relative).normalize();if(!p.startsWith(root)||p.startsWith(trash))throw StaticResourceValidationException("静态资源路径越界");return p}
 private fun entry(p:Path)=StaticEntry(root.relativize(p).toString().replace('\\','/'),p.fileName.toString(),Files.isDirectory(p),if(Files.isRegularFile(p))Files.size(p) else 0,runCatching{Files.getLastModifiedTime(p).toInstant()}.getOrNull())
}

@RestController @RequestMapping("/api/admin/static-resources")
class AdminStaticResourceController(private val service:StaticResourceService){
 @GetMapping fun list(@RequestParam(defaultValue="") path:String)=service.list(path)
 @PostMapping(consumes=[MediaType.MULTIPART_FORM_DATA_VALUE]) fun upload(@RequestParam path:String,@RequestPart file:MultipartFile,@RequestParam(defaultValue="false") replace:Boolean)=service.upload(path,file,replace)
 @DeleteMapping fun delete(@RequestParam path:String)=service.delete(path)
 @GetMapping("/trash") fun trash()=service.trash()
 @PostMapping("/restore/{id}") fun restore(@PathVariable id:String)=service.restore(id)
}

@RestController
class PublicStaticResourceController(private val service:StaticResourceService){
 @GetMapping("/static/**") fun get(request:HttpServletRequest):ResponseEntity<FileSystemResource>{val path=request.requestURI.substringAfter("/static/");val file=service.resolvePublic(path);val media=runCatching{MediaType.parseMediaType(Files.probeContentType(file)?:"application/octet-stream")}.getOrDefault(MediaType.APPLICATION_OCTET_STREAM);return ResponseEntity.ok().contentType(media).body(FileSystemResource(file))}
}
