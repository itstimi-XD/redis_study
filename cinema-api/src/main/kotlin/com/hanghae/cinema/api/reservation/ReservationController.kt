package com.hanghae.cinema.api.reservation

import com.hanghae.cinema.api.annotation.RateLimit
import com.hanghae.cinema.api.annotation.RateLimitType
import com.hanghae.cinema.api.response.ApiResponse
import com.hanghae.cinema.application.reservation.ReservationFacade
import com.hanghae.cinema.application.reservation.dto.ReservationRequest
import com.hanghae.cinema.application.reservation.dto.ReservationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "예약 API")
class ReservationController(
    private val reservationFacade: ReservationFacade
) {
    @PostMapping
    @RateLimit(type = RateLimitType.BOOKING)
    @Operation(summary = "영화 예약", description = "영화 좌석을 예약합니다.")
    fun reserve(
        @RequestHeader("X-USER-ID") userId: String,
        @RequestBody request: ReservationRequest
    ): ApiResponse<List<ReservationResponse>> {
        val reservations = reservationFacade.reserve(request, userId)
        return ApiResponse.success(reservations)
    }
} 