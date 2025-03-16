package com.hanghae.cinema.application.movie

import com.hanghae.cinema.application.movie.dto.MovieResponseDto
import com.hanghae.cinema.application.movie.dto.ScheduleResponseDto
import com.hanghae.cinema.domain.movie.MovieService
import com.hanghae.cinema.domain.schedule.ScheduleService
import org.springframework.stereotype.Service

@Service
class MovieFacade(
    private val movieService: MovieService,
    private val scheduleService: ScheduleService
) {
    
    fun getNowPlayingMovies(): List<MovieResponseDto> {
        val nowPlayingMovies = movieService.findNowPlayingMovies()
        return nowPlayingMovies.map { movie ->
            val schedules = scheduleService.findByMovie(movie.id!!)
            MovieResponseDto(
                id = movie.id,
                title = movie.title,
                rating = movie.rating,
                releaseDate = movie.releaseDate,
                thumbnailUrl = movie.thumbnailUrl,
                runningTime = movie.runningTime,
                genre = movie.genre.name,
                schedules = schedules.map { schedule ->
                    ScheduleResponseDto(
                        id = schedule.id!!,
                        theaterName = schedule.theater.name,
                        startTime = schedule.startTime,
                        endTime = schedule.endTime
                    )
                }.sortedBy { it.startTime }
            )
        }.sortedByDescending { it.releaseDate }
    }
} 