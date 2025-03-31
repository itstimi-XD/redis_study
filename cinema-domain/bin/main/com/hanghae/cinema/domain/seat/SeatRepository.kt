package com.hanghae.cinema.domain.seat

interface SeatRepository {
    fun findById(id: Long): Seat?
    fun findAllById(ids: List<Long>): List<Seat>
} 