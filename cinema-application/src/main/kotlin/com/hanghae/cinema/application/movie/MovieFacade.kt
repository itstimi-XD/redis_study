package com.hanghae.cinema.application.movie

import com.hanghae.cinema.application.movie.dto.MovieResponseDto
import com.hanghae.cinema.application.movie.dto.ScheduleResponseDto
import com.hanghae.cinema.domain.movie.MovieService
import com.hanghae.cinema.domain.schedule.ScheduleService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class MovieFacade(
    private val movieService: MovieService,
    private val scheduleService: ScheduleService
) {
    
    @Cacheable(
        value = ["nowPlayingMovies"], 
        key = "#title?.toString() ?: 'all' + '_' + #genre?.toString() ?: 'all'"
    )
    fun getNowPlayingMovies(title: String? = null, genre: String? = null): List<MovieResponseDto> {
        val nowPlayingMovies = movieService.findNowPlayingMovies(title, genre)
        return nowPlayingMovies.mapNotNull { movie ->
            movie.id?.let { movieId ->
                val schedules = scheduleService.findByMovie(movieId)
                MovieResponseDto(
                    id = movieId,
                    title = movie.title,
                    rating = movie.rating,
                    releaseDate = movie.releaseDate,
                    thumbnailUrl = movie.thumbnailUrl,
                    runningTime = movie.runningTime,
                    genre = movie.genre.name,
                    schedules = schedules.mapNotNull { schedule ->
                        schedule.id?.let { scheduleId ->
                            ScheduleResponseDto(
                                id = scheduleId,
                                theaterName = schedule.theater.name,
                                startTime = schedule.startTime,
                                endTime = schedule.endTime
                            )
                        }
                    }.sortedBy { it.startTime }
                )
            }
        }.sortedByDescending { it.releaseDate }
    }
} 