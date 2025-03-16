package com.hanghae.cinema.infrastructure.persistence.movie

import com.hanghae.cinema.domain.movie.Movie
import com.hanghae.cinema.domain.movie.MovieRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MovieRepositoryImpl(private val movieJpaRepository: MovieJpaRepository) : MovieRepository {
    
    override fun findByReleaseDateLessThanEqualOrderByReleaseDateDesc(date: LocalDate): List<Movie> {
        return movieJpaRepository.findByReleaseDateLessThanEqualOrderByReleaseDateDesc(date)
    }
} 