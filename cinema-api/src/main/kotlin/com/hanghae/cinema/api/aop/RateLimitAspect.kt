package com.hanghae.cinema.api.aop

import com.hanghae.cinema.api.annotation.RateLimit
import com.hanghae.cinema.api.annotation.RateLimitType
import com.hanghae.cinema.api.ratelimit.RateLimiter
import com.hanghae.cinema.api.exception.RateLimitExceededException
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class RateLimitAspect(
    private val request: HttpServletRequest,
    private val rateLimiter: RateLimiter
) {
    @Around("@annotation(rateLimit)")
    fun rateLimitCheck(joinPoint: ProceedingJoinPoint, rateLimit: RateLimit): Any {
        val clientIp = request.remoteAddr

        // IP가 차단되었는지 먼저 확인
        if (rateLimiter.isBlocked(clientIp)) {
            throw RateLimitExceededException("과도한 요청으로 인해 1시간 동안 차단되었습니다.")
        }

        when (rateLimit.type) {
            RateLimitType.API_CALL -> rateLimiter.checkApiRateLimit(clientIp)
            RateLimitType.BOOKING -> {
                val scheduleId = joinPoint.args[0].toString().toLong()
                rateLimiter.checkBookingRateLimit(clientIp, scheduleId)
            }
        }

        return joinPoint.proceed()
    }
} 