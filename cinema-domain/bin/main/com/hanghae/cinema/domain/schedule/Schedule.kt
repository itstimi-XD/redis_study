package com.hanghae.cinema.domain.schedule

import com.hanghae.cinema.domain.movie.Movie
import com.hanghae.cinema.domain.theater.Theater
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "schedules")
class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    val movie: Movie,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id")
    val theater: Theater,
    
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String = "system",
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var updatedBy: String = "system"
) 