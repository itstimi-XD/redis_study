package com.hanghae.cinema.domain.movie

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MovieService(private val movieRepository: MovieRepository) {
    
    fun findNowPlayingMovies(): List<Movie> {
        val today = LocalDate.now()
        return movieRepository.findByReleaseDateLessThanEqualOrderByReleaseDateDesc(today)
    }
} 