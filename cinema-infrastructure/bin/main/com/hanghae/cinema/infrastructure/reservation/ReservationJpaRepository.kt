package com.hanghae.cinema.infrastructure.reservation

import com.hanghae.cinema.domain.reservation.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import jakarta.persistence.LockModeType

interface ReservationJpaRepository : JpaRepository<Reservation, Long> {
    fun findByScheduleIdAndSeatId(scheduleId: Long, seatId: Long): Reservation?
    
    fun findByScheduleIdAndUserId(scheduleId: Long, userId: String): List<Reservation>
    
    fun findByScheduleId(scheduleId: Long): List<Reservation>
    
    fun findAllByScheduleIdAndSeatIdIn(scheduleId: Long, seatIds: List<Long>): List<Reservation>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.schedule.id = :scheduleId AND r.seat.id IN :seatIds")
    fun findAllByScheduleIdAndSeatIdInWithPessimisticLock(scheduleId: Long, seatIds: List<Long>): List<Reservation>
} 