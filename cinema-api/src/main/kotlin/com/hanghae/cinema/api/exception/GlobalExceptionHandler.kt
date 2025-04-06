package com.hanghae.cinema.api.exception

import com.hanghae.cinema.api.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(RateLimitExceededException::class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun handleRateLimitExceededException(e: RateLimitExceededException): ApiResponse<Nothing> {
        return ApiResponse.error(
            status = HttpStatus.TOO_MANY_REQUESTS.value(),
            code = "RATE_LIMIT_EXCEEDED",
            message = e.message ?: "Rate limit exceeded"
        )
    }
    
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ApiResponse<Nothing> {
        return ApiResponse.error(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred"
        )
    }
} 