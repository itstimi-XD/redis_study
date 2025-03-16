package com.hanghae.cinema.domain.movie

import java.time.LocalDate

interface MovieRepository {
    fun findByReleaseDateLessThanEqualOrderByReleaseDateDesc(date: LocalDate): List<Movie>
} 