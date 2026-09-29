package com.jilinjobs.cms.resource

import org.springframework.web.multipart.MultipartFile

fun MultipartFile.toUploadContent(): UploadContent =
    UploadContent(originalFilename, contentType, size) { inputStream }
