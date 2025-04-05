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

    @Column(name = "start_time")
    val startTime: LocalDateTime,

    @Column(name = "end_time")
    val endTime: LocalDateTime,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by")
    val createdBy: String = "system",

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: String = "system"
) 