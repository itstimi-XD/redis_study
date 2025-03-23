package com.hanghae.cinema.domain.movie

import java.time.LocalDate

interface MovieRepository {
    fun findByReleaseDateLessThanEqualOrderByReleaseDateDesc(date: LocalDate): List<Movie>
    fun findNowPlayingMoviesWithFilters(title: String?, genre: String?): List<Movie>
    fun saveAll(movies: List<Movie>): List<Movie>
    fun save(movie: Movie): Movie
    fun findById(id: Long): Movie?
} 