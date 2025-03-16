package com.hanghae.cinema.infrastructure.persistence.schedule

import com.hanghae.cinema.domain.schedule.Schedule
import org.springframework.data.jpa.repository.JpaRepository

interface ScheduleJpaRepository : JpaRepository<Schedule, Long> {
    fun findByMovieId(movieId: Long): List<Schedule>
} 