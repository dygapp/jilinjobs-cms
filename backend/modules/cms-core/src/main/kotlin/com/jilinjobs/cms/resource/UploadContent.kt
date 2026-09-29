package com.jilinjobs.cms.resource

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

/** Application-neutral upload input; each access opens a fresh stream. */
class UploadContent(
    val originalFilename: String?,
    val contentType: String?,
    val size: Long,
    private val openStream: () -> InputStream,
) {
    init {
        require(size >= 0) { "上传文件大小不能为负数" }
    }

    val isEmpty: Boolean get() = size == 0L
    val inputStream: InputStream get() = openStream()

    companion object {
        fun fromPath(path: Path, originalFilename: String, contentType: String?): UploadContent =
            UploadContent(originalFilename, contentType, Files.size(path)) { Files.newInputStream(path) }
    }
}
