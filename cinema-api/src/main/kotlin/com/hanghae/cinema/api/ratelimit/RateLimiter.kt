package com.hanghae.cinema.api.ratelimit

interface RateLimiter {
    /**
     * API 호출 제한을 확인합니다.
     * @param ip 클라이언트 IP
     * @return 제한에 걸리지 않으면 true
     * @throws RateLimitExceededException 제한에 걸린 경우
     */
    fun checkApiRateLimit(ip: String): Boolean

    /**
     * 예약 제한을 확인합니다.
     * @param ip 클라이언트 IP
     * @param scheduleId 상영 일정 ID
     * @return 제한에 걸리지 않으면 true
     * @throws RateLimitExceededException 제한에 걸린 경우
     */
    fun checkBookingRateLimit(ip: String, scheduleId: Long): Boolean

    /**
     * IP가 차단되었는지 확인합니다.
     * @param ip 클라이언트 IP
     * @return 차단된 경우 true
     */
    fun isBlocked(ip: String): Boolean
} 