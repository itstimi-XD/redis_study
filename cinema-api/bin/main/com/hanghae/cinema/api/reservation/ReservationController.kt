package com.hanghae.cinema.api.reservation

import com.hanghae.cinema.application.reservation.ReservationFacade
import com.hanghae.cinema.application.reservation.dto.ReservationRequest
import com.hanghae.cinema.application.reservation.dto.ReservationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationFacade: ReservationFacade
) {
    @PostMapping
    fun reserve(
        @RequestHeader("X-USER-ID") userId: String,
        @RequestBody request: ReservationRequest
    ): ResponseEntity<List<ReservationResponse>> {
        val reservations = reservationFacade.reserve(request, userId)
        return ResponseEntity.ok(reservations)
    }
} 