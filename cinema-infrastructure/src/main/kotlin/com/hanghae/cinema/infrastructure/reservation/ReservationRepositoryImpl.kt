package com.hanghae.cinema.infrastructure.reservation

import com.hanghae.cinema.domain.reservation.Reservation
import com.hanghae.cinema.domain.reservation.ReservationRepository
import com.hanghae.cinema.domain.reservation.PessimisticLockableReservationRepository
import org.springframework.stereotype.Repository

@Repository
class ReservationRepositoryImpl(
    private val reservationJpaRepository: ReservationJpaRepository
) : ReservationRepository, PessimisticLockableReservationRepository {
    override fun save(reservation: Reservation): Reservation {
        return reservationJpaRepository.save(reservation)
    }

    override fun findByScheduleIdAndSeatId(scheduleId: Long, seatId: Long): Reservation? {
        return reservationJpaRepository.findByScheduleIdAndSeatId(scheduleId, seatId)
    }

    override fun findByScheduleIdAndUserId(scheduleId: Long, userId: String): List<Reservation> {
        return reservationJpaRepository.findByScheduleIdAndUserId(scheduleId, userId)
    }

    override fun findByScheduleId(scheduleId: Long): List<Reservation> {
        return reservationJpaRepository.findByScheduleId(scheduleId)
    }

    override fun findAllByScheduleIdAndSeatIdIn(scheduleId: Long, seatIds: List<Long>): List<Reservation> {
        return reservationJpaRepository.findAllByScheduleIdAndSeatIdIn(scheduleId, seatIds)
    }

    override fun findAllByScheduleIdAndSeatIdInWithPessimisticLock(scheduleId: Long, seatIds: List<Long>): List<Reservation> {
        return reservationJpaRepository.findAllByScheduleIdAndSeatIdInWithPessimisticLock(scheduleId, seatIds)
    }
} 