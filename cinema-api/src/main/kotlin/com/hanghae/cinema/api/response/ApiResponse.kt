package com.hanghae.cinema.api.response

import org.springframework.http.HttpStatus

data class ApiResponse<T>(
    val status: Int,
    val code: String,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> success(data: T?, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                status = HttpStatus.OK.value(),
                code = "SUCCESS",
                message = message,
                data = data
            )
        }

        fun <T> error(status: Int, code: String, message: String): ApiResponse<T> {
            return ApiResponse(
                status = status,
                code = code,
                message = message,
                data = null
            )
        }
    }
} 