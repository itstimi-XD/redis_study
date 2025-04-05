package com.hanghae.cinema.domain.seat

import com.hanghae.cinema.domain.theater.Theater
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "seats")
class Seat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id")
    val theater: Theater,
    
    @Column(name = "seat_number")
    val seatNumber: String,
    
    @Column(name = "seat_row")
    val seatRow: Char,
    
    @Column(name = "column_num")
    val column: Int,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by")
    val createdBy: String = "system",

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: String = "system"
) 