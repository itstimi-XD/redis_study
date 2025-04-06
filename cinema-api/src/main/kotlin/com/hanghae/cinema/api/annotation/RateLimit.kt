package com.hanghae.cinema.api.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(
    val type: RateLimitType
)

enum class RateLimitType {
    API_CALL,    // 일반 API 호출 제한 (1분당 50회)
    BOOKING      // 예약 API 제한 (5분당 1회)
} 