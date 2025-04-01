package com.hanghae.cinema.application.movie.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class MovieResponseDto(
    val id: Long,
    val title: String,
    val rating: String,
    val releaseDate: LocalDate,
    val thumbnailUrl: String,
    val runningTime: Int,
    val genre: String,
    val schedules: List<ScheduleResponseDto>
)

data class ScheduleResponseDto(
    val id: Long,
    val theaterName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
) 