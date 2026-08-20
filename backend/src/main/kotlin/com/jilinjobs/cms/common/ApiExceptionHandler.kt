package com.jilinjobs.cms.common

import com.jilinjobs.cms.column.ColumnNotFoundException
import com.jilinjobs.cms.column.ColumnValidationException
import com.jilinjobs.cms.navigation.NavigationNotFoundException
import com.jilinjobs.cms.navigation.NavigationValidationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ColumnValidationException::class, NavigationValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(exception: RuntimeException): ApiError = ApiError(exception.message ?: "请求不合法")

    @ExceptionHandler(ColumnNotFoundException::class, NavigationNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(exception: RuntimeException): ApiError = ApiError(exception.message ?: "资源不存在")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBeanValidation(exception: MethodArgumentNotValidException): ApiError {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "请求参数不合法"
        return ApiError(message)
    }
}

data class ApiError(
    val message: String,
)
