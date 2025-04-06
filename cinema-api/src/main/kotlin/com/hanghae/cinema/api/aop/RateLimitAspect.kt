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

        when (rateLimit.type) {
            RateLimitType.API_CALL -> rateLimiter.checkApiCallLimit(clientIp)
            RateLimitType.BOOKING -> {
                val movieTimeId = joinPoint.args[0].toString()
                rateLimiter.checkBookingLimit(clientIp, movieTimeId)
            }
        }

        return joinPoint.proceed()
    }
} 