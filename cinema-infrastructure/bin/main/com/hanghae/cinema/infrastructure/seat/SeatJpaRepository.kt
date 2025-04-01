package com.hanghae.cinema.infrastructure.seat

import com.hanghae.cinema.domain.seat.Seat
import org.springframework.data.jpa.repository.JpaRepository

interface SeatJpaRepository : JpaRepository<Seat, Long> 