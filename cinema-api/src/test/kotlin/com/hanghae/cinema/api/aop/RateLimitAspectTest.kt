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
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ActiveProfiles

class RateLimitAspectTest {
    private lateinit var request: HttpServletRequest
    private lateinit var joinPoint: ProceedingJoinPoint
    private lateinit var rateLimit: RateLimit

    @BeforeEach
    fun setUp() {
        request = MockHttpServletRequest()
        joinPoint = mock(ProceedingJoinPoint::class.java)
        rateLimit = mock(RateLimit::class.java)
        `when`(joinPoint.proceed()).thenReturn("dummy result")
        (request as MockHttpServletRequest).remoteAddr = "127.0.0.1"
    }

    @Nested
    @ActiveProfiles("single")
    inner class GuavaRateLimitTest {
        private lateinit var rateLimitAspect: RateLimitAspect
        private lateinit var guavaRateLimiter: GuavaRateLimiter

        @BeforeEach
        fun setUp() {
            guavaRateLimiter = GuavaRateLimiter()
            rateLimitAspect = RateLimitAspect(request, guavaRateLimiter)
        }

        @Test
        fun `API 호출이 50회 이하면 정상 처리된다`() {
            // given
            `when`(rateLimit.type).thenReturn(RateLimitType.API_CALL)

            // when & then
            repeat(50) {
                val result = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
                assert(result == "dummy result")
            }
            verify(joinPoint, times(50)).proceed()
        }

        @Test
        fun `1분 내 API 호출이 50회를 초과하면 1시간 동안 차단된다`() {
            // given
            `when`(rateLimit.type).thenReturn(RateLimitType.API_CALL)

            // when & then
            // 50회 호출 (정상)
            repeat(50) {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            // 51번째 호출 (차단)
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            // 차단된 후 추가 호출
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            verify(joinPoint, times(50)).proceed()
        }

        @Test
        fun `예약 API는 같은 시간대에 5분 내 재호출이 불가능하다`() {
            // given
            `when`(rateLimit.type).thenReturn(RateLimitType.BOOKING)
            `when`(joinPoint.args).thenReturn(arrayOf("1")) // movieTimeId

            // when
            rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)

            // then
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }
            verify(joinPoint, times(1)).proceed()
        }

        @Test
        fun `예약 API는 다른 시간대 영화는 즉시 예약 가능하다`() {
            // given
            `when`(rateLimit.type).thenReturn(RateLimitType.BOOKING)
            
            // when & then
            // 첫 번째 시간대 예약
            `when`(joinPoint.args).thenReturn(arrayOf("1")) // 08:00 movieTimeId
            rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)

            // 다른 시간대는 바로 예약 가능
            `when`(joinPoint.args).thenReturn(arrayOf("2")) // 12:00 movieTimeId
            rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)

            verify(joinPoint, times(2)).proceed()
        }
    }

    @Nested
    @ActiveProfiles("distributed")
    inner class RedisRateLimitTest {
        private lateinit var rateLimitAspect: RateLimitAspect
        private lateinit var redissonClient: RedissonClient
        private lateinit var redisRateLimiter: RedisRateLimiter
        private lateinit var counter: RAtomicLong
        private lateinit var bucket: RBucket<Boolean>

        @BeforeEach
        fun setUp() {
            redissonClient = mock(RedissonClient::class.java)
            counter = mock(RAtomicLong::class.java)
            bucket = mock(RBucket::class.java) as RBucket<Boolean>
            redisRateLimiter = RedisRateLimiter(redissonClient)
            rateLimitAspect = RateLimitAspect(request, redisRateLimiter)
        }

        @Test
        fun `API 호출이 50회 이하면 정상 처리된다`() {
            // given
            `when`(rateLimit.type).thenReturn(RateLimitType.API_CALL)
            `when`(redissonClient.getBucket<Boolean>(anyString())).thenReturn(bucket)
            `when`(bucket.isExists).thenReturn(false)
            `when`(redissonClient.getAtomicLong(anyString())).thenReturn(counter)
            `when`(counter.isExists).thenReturn(true)
            `when`(counter.incrementAndGet()).thenReturn(50L)

            // when
            val result = rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)

            // then
            verify(joinPoint).proceed()
            assert(result == "dummy result")
        }

        @Test
        fun `1분 내 API 호출이 50회를 초과하면 1시간 동안 차단된다`() {
            // given
            `when`(rateLimit.type).thenReturn(RateLimitType.API_CALL)
            `when`(redissonClient.getBucket<Boolean>(anyString())).thenReturn(bucket)
            `when`(bucket.isExists).thenReturn(false)
            `when`(redissonClient.getAtomicLong(anyString())).thenReturn(counter)
            `when`(counter.isExists).thenReturn(true)
            `when`(counter.incrementAndGet()).thenReturn(51L)

            // when & then
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            // 차단 확인
            `when`(bucket.isExists).thenReturn(true)
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }

            verify(joinPoint, never()).proceed()
        }

        @Test
        fun `예약 API는 같은 시간대에 5분 내 재호출이 불가능하다`() {
            // given
            `when`(rateLimit.type).thenReturn(RateLimitType.BOOKING)
            `when`(redissonClient.getBucket<Boolean>(anyString())).thenReturn(bucket)
            `when`(bucket.isExists).thenReturn(true)
            `when`(joinPoint.args).thenReturn(arrayOf("1")) // movieTimeId

            // when & then
            assertThrows<RateLimitExceededException> {
                rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)
            }
            verify(joinPoint, never()).proceed()
        }

        @Test
        fun `예약 API는 다른 시간대 영화는 즉시 예약 가능하다`() {
            // given
            `when`(rateLimit.type).thenReturn(RateLimitType.BOOKING)
            `when`(redissonClient.getBucket<Boolean>(anyString())).thenReturn(bucket)
            
            // when & then
            // 첫 번째 시간대 예약
            `when`(bucket.isExists).thenReturn(false)
            `when`(joinPoint.args).thenReturn(arrayOf("1")) // 08:00 movieTimeId
            rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)

            // 다른 시간대는 바로 예약 가능
            `when`(joinPoint.args).thenReturn(arrayOf("2")) // 12:00 movieTimeId
            rateLimitAspect.rateLimitCheck(joinPoint, rateLimit)

            verify(joinPoint, times(2)).proceed()
        }
    }
} 