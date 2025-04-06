package com.hanghae.cinema.api.reservation

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var reservationFacade: ReservationFacade

    @Test
    fun `예약 API가 정상적으로 동작한다`() {
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
        mockMvc.perform(post("/api/reservations")
            .header("X-USER-ID", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].movieTitle").value("Test Movie"))
    }

    @Test
    fun `사용자 ID가 없으면 400 에러가 발생한다`() {
        // given
        val request = ReservationRequest(
            scheduleId = 1L,
            seatIds = listOf(1L, 2L)
        )

        // when & then
        mockMvc.perform(post("/api/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
    }
} 