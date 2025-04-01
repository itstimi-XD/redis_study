package com.hanghae.cinema.infrastructure.seat

import com.hanghae.cinema.domain.seat.Seat
import com.hanghae.cinema.domain.seat.SeatRepository
import org.springframework.stereotype.Repository

@Repository
class SeatRepositoryImpl(
    private val seatJpaRepository: SeatJpaRepository
) : SeatRepository {
    override fun findById(id: Long): Seat? {
        return seatJpaRepository.findById(id).orElse(null)
    }

    override fun findAllById(ids: List<Long>): List<Seat> {
        return seatJpaRepository.findAllById(ids)
    }
} 