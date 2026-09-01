package com.jilinjobs.cms.common

import com.jilinjobs.cms.advertisement.*
import com.jilinjobs.cms.column.*
import com.jilinjobs.cms.content.*
import com.jilinjobs.cms.listing.*
import com.jilinjobs.cms.navigation.*
import com.jilinjobs.cms.page.*
import com.jilinjobs.cms.resource.*
import com.jilinjobs.cms.siteconfig.*
import com.jilinjobs.cms.staticresource.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(
        ColumnValidationException::class,
        NavigationValidationException::class,
        ArticleValidationException::class,
        ResourceValidationException::class,
        PageValidationException::class,
        SiteConfigValidationException::class,
        StaticResourceValidationException::class,
        CmsListValidationException::class,
        AdvertisementValidationException::class,
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun validation(e: RuntimeException) = ApiError(e.message ?: "请求不合法")

    @ExceptionHandler(
        ColumnNotFoundException::class,
        ColumnAliasNotFoundException::class,
        NavigationNotFoundException::class,
        NavigationLocationNotFoundException::class,
        ArticleNotFoundException::class,
        ResourceNotFoundException::class,
        PageNotFoundException::class,
        SiteConfigNotFoundException::class,
        StaticResourceNotFoundException::class,
        CmsListNotFoundException::class,
        CmsListItemNotFoundException::class,
        AdvertisementSlotNotFoundException::class,
        AdvertisementNotFoundException::class,
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun notFound(e: RuntimeException) = ApiError(e.message ?: "资源不存在")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun bean(e: MethodArgumentNotValidException) = ApiError(e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "请求参数不合法")
}

data class ApiError(val message: String)
