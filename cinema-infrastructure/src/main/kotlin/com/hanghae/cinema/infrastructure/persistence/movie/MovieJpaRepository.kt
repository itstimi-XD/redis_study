package com.hanghae.cinema.infrastructure.persistence.movie

import com.hanghae.cinema.domain.movie.Movie
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface MovieJpaRepository : JpaRepository<Movie, Long> {
    fun findByReleaseDateLessThanEqualOrderByReleaseDateDesc(date: LocalDate): List<Movie>
} 