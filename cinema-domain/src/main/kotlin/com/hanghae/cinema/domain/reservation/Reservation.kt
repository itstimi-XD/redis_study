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

    @Column(name = "user_id")
    val userId: String,  // 사용자 식별자

    @Version  // Optimistic Lock을 위한 버전 필드
    @Column(name = "version")
    var version: Long = 0,

    @Column(name = "reserved_at")
    val reservedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by")
    val createdBy: String = "system",

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: String = "system"
) 