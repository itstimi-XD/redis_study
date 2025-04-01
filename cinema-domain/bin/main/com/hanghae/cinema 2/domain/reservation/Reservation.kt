package com.hanghae.cinema.domain.reservation

import com.hanghae.cinema.domain.schedule.Schedule
import com.hanghae.cinema.domain.seat.Seat
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reservations")
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    val schedule: Schedule,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    val seat: Seat,

    val userId: String,  // 사용자 식별자

    @Version  // Optimistic Lock을 위한 버전 필드
    var version: Long = 0,

    val reservedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String = "system",
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var updatedBy: String = "system"
) 