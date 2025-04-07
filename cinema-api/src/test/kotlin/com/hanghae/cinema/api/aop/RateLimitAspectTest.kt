package com.hanghae.cinema.api.aop

import com.hanghae.cinema.api.annotation.RateLimit
import com.hanghae.cinema.api.annotation.RateLimitType
import com.hanghae.cinema.api.exception.RateLimitExceededException
import com.hanghae.cinema.api.ratelimit.GuavaRateLimiter
import com.hanghae.cinema.api.ratelimit.RedisRateLimiter
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.redisson.api.RAtomicLong
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient
import org.redisson.api.RScript
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ActiveProfiles
import org.mockito.ArgumentMatcher
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class RateLimitAspectTest {
    private lateinit var request: HttpServletRequest
    private lateinit var joinPoint: ProceedingJoinPoint
    private lateinit var rateLimit: RateLimit
    private val testIp = "127.0.0.1"

    @BeforeEach
    fun setUp() {
        request = MockHttpServletRequest().apply {
            remoteAddr = testIp
        }
        joinPoint = mock()
        rateLimit = mock()
        whenever(joinPoint.proceed()).thenReturn("dummy result")
    }

    @Nested
    @ActiveProfiles("single")
    inner class GuavaRateLimitTest {
        private lateinit var rateLimitAspect: RateLimitAspect
        private lateinit var guavaRateLimiter: GuavaRateLimiter

        @BeforeEach
        fun setUp() {
            guavaRateLimiter = spy(GuavaRateLimiter())
            rateLimitAspect = RateLimitAspect(request, guavaRateLimiter)
        }

        @Test
        fun `API 호출이 50회 이하면 정상 처리된다`() {
            // given
            whenever(rateLimit.type).thenReturn(RateLimitType.API_CALL)
            whenever(guavaRateLimiter.isBlocked(testIp)).thenReturn(false)
            whenever(guavaRateLimiter.checkApiRateLimit(testIp)).thenReturn(true)

            // when
            repeat(50) { // 50회 호출 (50회까지 허용)
                val result = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
                assert(result == "dummy result")
            }

            // then
            verify(joinPoint, times(50)).proceed()
            verify(guavaRateLimiter, times(50)).checkApiRateLimit(testIp)
        }

        @Test
        fun `1분 내 API 호출이 50회를 초과하면 1시간 동안 차단된다`() {
            // given
            whenever(rateLimit.type).thenReturn(RateLimitType.API_CALL)
            whenever(guavaRateLimiter.isBlocked(testIp)).thenReturn(false)
            whenever(guavaRateLimiter.checkApiRateLimit(testIp))
                .thenReturn(true) // 처음 50회는 true
                .thenReturn(false) // 51번째에서 false

            // when & then
            repeat(50) { // 50회 성공
                val result = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
                assert(result == "dummy result")
            }

            // 51번째 호출에서 실패
            whenever(guavaRateLimiter.isBlocked(testIp)).thenReturn(true)
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            verify(joinPoint, times(50)).proceed()
            verify(guavaRateLimiter, times(50)).checkApiRateLimit(testIp)
        }

        @Test
        fun `예약 API는 같은 시간대에 5분 내 재호출이 불가능하다`() {
            // given
            whenever(rateLimit.type).thenReturn(RateLimitType.BOOKING)
            whenever(guavaRateLimiter.isBlocked(testIp)).thenReturn(false)
            whenever(joinPoint.args).thenReturn(arrayOf("1")) // scheduleId

            // 첫 번째 호출은 성공
            whenever(guavaRateLimiter.checkBookingRateLimit(eq(testIp), eq(1L)))
                .thenReturn(true) // 첫 번째 호출
                .thenReturn(false) // 두 번째 호출

            // when & then
            // 첫 번째 예약 성공
            val result = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            assert(result == "dummy result")

            // 같은 시간대 두 번째 예약 실패
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            verify(joinPoint, times(1)).proceed()
            verify(guavaRateLimiter, times(2)).checkBookingRateLimit(testIp, 1L)
        }

        @Test
        fun `예약 API는 다른 시간대 영화는 즉시 예약 가능하다`() {
            // given
            whenever(rateLimit.type).thenReturn(RateLimitType.BOOKING)
            whenever(guavaRateLimiter.isBlocked(testIp)).thenReturn(false)
            
            // when & then
            // 첫 번째 시간대 예약
            whenever(joinPoint.args).thenReturn(arrayOf("1")) // 첫 번째 scheduleId
            whenever(guavaRateLimiter.checkBookingRateLimit(eq(testIp), eq(1L))).thenReturn(true)
            val result1 = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            assert(result1 == "dummy result")

            // 다른 시간대는 바로 예약 가능
            whenever(joinPoint.args).thenReturn(arrayOf("2")) // 다른 scheduleId
            whenever(guavaRateLimiter.checkBookingRateLimit(eq(testIp), eq(2L))).thenReturn(true)
            val result2 = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            assert(result2 == "dummy result")

            verify(joinPoint, times(2)).proceed()
            verify(guavaRateLimiter, times(1)).checkBookingRateLimit(testIp, 1L)
            verify(guavaRateLimiter, times(1)).checkBookingRateLimit(testIp, 2L)
        }
    }

    @Nested
    @ActiveProfiles("distributed")
    inner class RedisRateLimitTest {
        private lateinit var rateLimitAspect: RateLimitAspect
        private lateinit var redissonClient: RedissonClient
        private lateinit var redisTemplate: RedisTemplate<String, Any>
        private lateinit var redisRateLimiter: RedisRateLimiter
        private lateinit var script: RScript
        private lateinit var bucket: RBucket<Boolean>

        @BeforeEach
        fun setUp() {
            redissonClient = mock()
            redisTemplate = mock()
            script = mock()
            bucket = mock()
            
            whenever(redissonClient.getScript()).thenReturn(script)
            whenever(redissonClient.getBucket<Boolean>(any())).thenReturn(bucket)
            whenever(bucket.isExists).thenReturn(false)
            
            redisRateLimiter = spy(RedisRateLimiter(redisTemplate, redissonClient))
            rateLimitAspect = RateLimitAspect(request, redisRateLimiter)
        }

        @Test
        fun `API 호출이 50회 이하면 정상 처리된다`() {
            // given
            whenever(rateLimit.type).thenReturn(RateLimitType.API_CALL)
            whenever(redisRateLimiter.isBlocked(testIp)).thenReturn(false)
            whenever(redisRateLimiter.checkApiRateLimit(testIp)).thenReturn(true)

            // when
            repeat(50) { // 50회 호출 (50회까지 허용)
                val result = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
                assert(result == "dummy result")
            }

            // then
            verify(joinPoint, times(50)).proceed()
            verify(redisRateLimiter, times(50)).checkApiRateLimit(testIp)
        }

        @Test
        fun `1분 내 API 호출이 50회를 초과하면 1시간 동안 차단된다`() {
            // given
            whenever(rateLimit.type).thenReturn(RateLimitType.API_CALL)
            whenever(redisRateLimiter.isBlocked(testIp))
                .thenReturn(false) // 처음에는 차단되지 않음
                .thenReturn(true)  // 51회 초과 후 차단됨
            
            whenever(redisRateLimiter.checkApiRateLimit(testIp))
                .thenReturn(true) // 처음 50회는 true
                .thenReturn(false) // 51번째에서 false

            // when & then
            repeat(50) { // 50회 성공
                val result = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
                assert(result == "dummy result")
            }

            // 51번째 호출에서 실패
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            verify(joinPoint, times(50)).proceed()
            verify(redisRateLimiter, times(50)).checkApiRateLimit(testIp)
        }

        @Test
        fun `예약 API는 같은 시간대에 5분 내 재호출이 불가능하다`() {
            // given
            whenever(rateLimit.type).thenReturn(RateLimitType.BOOKING)
            whenever(redisRateLimiter.isBlocked(testIp)).thenReturn(false)
            whenever(joinPoint.args).thenReturn(arrayOf("1")) // scheduleId

            // 첫 번째 호출은 성공, 두 번째 호출은 실패
            whenever(redisRateLimiter.checkBookingRateLimit(eq(testIp), eq(1L)))
                .thenReturn(true) // 첫 번째 호출
                .thenReturn(false) // 두 번째 호출

            // when & then
            // 첫 번째 예약 성공
            val result = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            assert(result == "dummy result")

            // 같은 시간대 두 번째 예약 실패
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            verify(joinPoint, times(1)).proceed()
            verify(redisRateLimiter, times(2)).checkBookingRateLimit(testIp, 1L)
        }

        @Test
        fun `예약 API는 다른 시간대 영화는 즉시 예약 가능하다`() {
            // given
            whenever(rateLimit.type).thenReturn(RateLimitType.BOOKING)
            whenever(redisRateLimiter.isBlocked(testIp)).thenReturn(false)
            
            // when & then
            // 첫 번째 시간대 예약
            whenever(joinPoint.args).thenReturn(arrayOf("1")) // 첫 번째 scheduleId
            whenever(redisRateLimiter.checkBookingRateLimit(eq(testIp), eq(1L))).thenReturn(true)
            val result1 = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            assert(result1 == "dummy result")

            // 다른 시간대는 바로 예약 가능
            whenever(joinPoint.args).thenReturn(arrayOf("2")) // 다른 scheduleId
            whenever(redisRateLimiter.checkBookingRateLimit(eq(testIp), eq(2L))).thenReturn(true)
            val result2 = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            assert(result2 == "dummy result")

            verify(joinPoint, times(2)).proceed()
            verify(redisRateLimiter, times(1)).checkBookingRateLimit(testIp, 1L)
            verify(redisRateLimiter, times(1)).checkBookingRateLimit(testIp, 2L)
        }
    }
} 