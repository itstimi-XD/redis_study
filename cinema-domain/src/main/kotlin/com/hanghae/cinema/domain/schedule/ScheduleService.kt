package com.hanghae.cinema.domain.schedule

import org.springframework.stereotype.Service

@Service
class ScheduleService(private val scheduleRepository: ScheduleRepository) {
    
    fun findByMovie(movieId: Long): List<Schedule> {
        return scheduleRepository.findByMovieId(movieId)
    }
} 