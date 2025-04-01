package com.hanghae.cinema.domain.theater

import com.hanghae.cinema.domain.schedule.Schedule
import com.hanghae.cinema.domain.seat.Seat
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "theaters")
class Theater(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    val name: String,
    val totalSeats: Int,
    
    @OneToMany(mappedBy = "theater")
    val schedules: MutableList<Schedule> = mutableListOf(),
    
    @OneToMany(mappedBy = "theater")
    val seats: MutableList<Seat> = mutableListOf(),
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String = "system",
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var updatedBy: String = "system"
) 