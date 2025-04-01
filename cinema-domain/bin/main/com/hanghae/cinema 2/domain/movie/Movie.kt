package com.hanghae.cinema.domain.movie

import com.hanghae.cinema.domain.genre.Genre
import com.hanghae.cinema.domain.schedule.Schedule
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "movies")
class Movie(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    val title: String,
    val rating: String,
    val releaseDate: LocalDate,
    val thumbnailUrl: String,
    val runningTime: Int,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    val genre: Genre,
    
    @OneToMany(mappedBy = "movie")
    val schedules: MutableList<Schedule> = mutableListOf(),
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String = "system",
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var updatedBy: String = "system"
) 