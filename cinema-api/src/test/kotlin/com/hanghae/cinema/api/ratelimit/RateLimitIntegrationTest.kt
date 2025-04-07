package com.hanghae.cinema.api.ratelimit

import com.hanghae.cinema.application.movie.MovieFacade
import com.hanghae.cinema.application.movie.dto.MovieResponseDto
import com.hanghae.cinema.application.reservation.ReservationFacade
import com.hanghae.cinema.application.reservation.dto.ReservationRequest
import com.hanghae.cinema.application.reservation.dto.ReservationResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @MockBean
    private lateinit var movieFacade: MovieFacade

    @MockBean
    private lateinit var reservationFacade: ReservationFacade

    @BeforeEach
    fun setUp() {
        // 테스트 전에 Redis의 모든 키를 삭제
        redisTemplate.connectionFactory?.connection?.flushAll()
    }

    @Test
    fun `API 호출 제한을 초과하면 429 에러가 발생한다`() {
        // given
        val movies = listOf(
            MovieResponseDto(
                id = 1L,
                title = "Test Movie",
                rating = "12세 이상",
                releaseDate = LocalDate.now(),
                thumbnailUrl = "http://example.com/image.jpg",
                runningTime = 120,
                genre = "액션",
                schedules = emptyList()
            )
        )
        `when`(movieFacade.getNowPlayingMovies(null, null)).thenReturn(movies)

        // when & then
        repeat(51) { count ->
            val result = mockMvc.perform(
                get("/api/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Forwarded-For", "127.0.0.1") // IP 주소 설정
            )
            
            if (count < 50) {
                result.andExpect(status().isOk)
            } else {
                result.andExpect(status().isTooManyRequests)
            }
        }
    }

    @Test
    fun `차단된 IP는 1시간 동안 모든 API 호출이 차단된다`() {
        // given
        val movies = listOf(
            MovieResponseDto(
                id = 1L,
                title = "Test Movie",
                rating = "12세 이상",
                releaseDate = LocalDate.now(),
                thumbnailUrl = "http://example.com/image.jpg",
                runningTime = 120,
                genre = "액션",
                schedules = emptyList()
            )
        )
        `when`(movieFacade.getNowPlayingMovies(null, null)).thenReturn(movies)

        // IP를 차단 상태로 만듦
        repeat(51) {
            mockMvc.perform(
                get("/api/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Forwarded-For", "127.0.0.2")
            )
        }

        // when & then
        // 차단된 후 추가 요청
        mockMvc.perform(
            get("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-For", "127.0.0.2")
        ).andExpect(status().isTooManyRequests)
    }

    @Test
    fun `같은 시간대의 영화를 5분 내에 재예약하면 429 에러가 발생한다`() {
        // given
        val userId = "testUser"
        val request = ReservationRequest(
            scheduleId = 1L,
            seatIds = listOf(1L, 2L)
        )
        val now = LocalDateTime.now()
        val response = listOf(
            ReservationResponse(
                id = 1L,
                scheduleId = 1L,
                movieTitle = "Test Movie",
                theaterName = "Test Theater",
                seatNumber = "A1",
                startTime = now,
                endTime = now.plusHours(2),
                reservedAt = now
            )
        )
        `when`(reservationFacade.reserve(request, userId)).thenReturn(response)

        // when & then
        // 첫 번째 예약
        mockMvc.perform(
            post("/api/reservations")
                .header("X-USER-ID", userId)
                .header("X-Forwarded-For", "127.0.0.3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk)

        // 같은 시간대 재예약 시도
        mockMvc.perform(
            post("/api/reservations")
                .header("X-USER-ID", userId)
                .header("X-Forwarded-For", "127.0.0.3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isTooManyRequests)
    }

    @Test
    fun `다른 시간대의 영화는 즉시 예약이 가능하다`() {
        // given
        val userId = "testUser"
        val now = LocalDateTime.now()
        
        val request1 = ReservationRequest(scheduleId = 1L, seatIds = listOf(1L))
        val response1 = listOf(
            ReservationResponse(
                id = 1L,
                scheduleId = 1L,
                movieTitle = "Test Movie 1",
                theaterName = "Test Theater",
                seatNumber = "A1",
                startTime = now,
                endTime = now.plusHours(2),
                reservedAt = now
            )
        )
        
        val request2 = ReservationRequest(scheduleId = 2L, seatIds = listOf(2L))
        val response2 = listOf(
            ReservationResponse(
                id = 2L,
                scheduleId = 2L,
                movieTitle = "Test Movie 2",
                theaterName = "Test Theater",
                seatNumber = "B1",
                startTime = now.plusHours(3),
                endTime = now.plusHours(5),
                reservedAt = now
            )
        )

        `when`(reservationFacade.reserve(request1, userId)).thenReturn(response1)
        `when`(reservationFacade.reserve(request2, userId)).thenReturn(response2)

        // when & then
        // 첫 번째 시간대 예약
        mockMvc.perform(
            post("/api/reservations")
                .header("X-USER-ID", userId)
                .header("X-Forwarded-For", "127.0.0.4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
        ).andExpect(status().isOk)

        // 다른 시간대 즉시 예약
        mockMvc.perform(
            post("/api/reservations")
                .header("X-USER-ID", userId)
                .header("X-Forwarded-For", "127.0.0.4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
        ).andExpect(status().isOk)
    }
} 