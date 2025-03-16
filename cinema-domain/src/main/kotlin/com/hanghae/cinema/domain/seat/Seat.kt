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
    
    val seatNumber: String,
    val row: Char,
    @Column(name = "column_num")
    val column: Int,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String = "system",
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var updatedBy: String = "system"
) 