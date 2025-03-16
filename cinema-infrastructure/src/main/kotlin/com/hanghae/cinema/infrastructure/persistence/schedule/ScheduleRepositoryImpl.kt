package com.hanghae.cinema.infrastructure.persistence.schedule

import com.hanghae.cinema.domain.schedule.Schedule
import com.hanghae.cinema.domain.schedule.ScheduleRepository
import org.springframework.stereotype.Repository

@Repository
class ScheduleRepositoryImpl(private val scheduleJpaRepository: ScheduleJpaRepository) : ScheduleRepository {
    
    override fun findByMovieId(movieId: Long): List<Schedule> {
        return scheduleJpaRepository.findByMovieId(movieId)
    }
} 