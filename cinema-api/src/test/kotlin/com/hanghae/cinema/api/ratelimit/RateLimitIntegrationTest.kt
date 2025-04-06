package com.hanghae.cinema.api.ratelimit

import com.hanghae.cinema.application.movie.MovieFacade
import com.hanghae.cinema.application.movie.dto.MovieResponseDto
import com.hanghae.cinema.application.reservation.ReservationFacade
import com.hanghae.cinema.application.reservation.dto.ReservationRequest
import com.hanghae.cinema.application.reservation.dto.ReservationResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
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

    @MockBean
    private lateinit var movieFacade: MovieFacade

    @MockBean
    private lateinit var reservationFacade: ReservationFacade

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
        repeat(51) {
            val result = mockMvc.perform(get("/api/movies")
                .contentType(MediaType.APPLICATION_JSON))
            
            if (it < 50) {
                result.andExpect(status().isOk)
            } else {
                result.andExpect(status().isTooManyRequests)
            }
        }
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
        mockMvc.perform(post("/api/reservations")
            .header("X-USER-ID", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)

        // 같은 시간대 재예약 시도
        mockMvc.perform(post("/api/reservations")
            .header("X-USER-ID", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isTooManyRequests)
    }
} 