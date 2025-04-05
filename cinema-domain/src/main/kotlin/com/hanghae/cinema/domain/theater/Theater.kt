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
    
    @Column(name = "name")
    val name: String,

    @Column(name = "total_seats")
    val totalSeats: Int,
    
    @OneToMany(mappedBy = "theater")
    val schedules: MutableList<Schedule> = mutableListOf(),
    
    @OneToMany(mappedBy = "theater")
    val seats: MutableList<Seat> = mutableListOf(),
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by")
    val createdBy: String = "system",

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: String = "system"
) 