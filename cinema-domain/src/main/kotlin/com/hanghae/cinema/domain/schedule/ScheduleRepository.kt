package com.hanghae.cinema.domain.schedule

interface ScheduleRepository {
    fun findByMovieId(movieId: Long): List<Schedule>
} 