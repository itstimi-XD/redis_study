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
    
    @Column(name = "title")
    val title: String,

    @Column(name = "rating")
    val rating: String,

    @Column(name = "release_date")
    val releaseDate: LocalDate,

    @Column(name = "thumbnail_url")
    val thumbnailUrl: String,

    @Column(name = "running_time")
    val runningTime: Int,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    val genre: Genre,
    
    @OneToMany(mappedBy = "movie")
    val schedules: MutableList<Schedule> = mutableListOf(),
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by")
    val createdBy: String = "system",

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: String = "system"
) 