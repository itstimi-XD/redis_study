package com.hanghae.cinema.infrastructure.persistence.movie

import com.hanghae.cinema.domain.movie.Movie
import com.hanghae.cinema.domain.movie.MovieRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MovieRepositoryAdapter(
    private val movieJpaRepository: MovieJpaRepository,
    private val movieRepositoryCustomImpl: MovieRepositoryCustomImpl
) : MovieRepository {
    
    override fun findByReleaseDateLessThanEqualOrderByReleaseDateDesc(date: LocalDate): List<Movie> {
        return movieJpaRepository.findByReleaseDateLessThanEqualOrderByReleaseDateDesc(date)
    }
    
    override fun findNowPlayingMoviesWithFilters(title: String?, genre: String?): List<Movie> {
        return movieRepositoryCustomImpl.findNowPlayingMoviesWithFilters(title, genre)
    }
    
    override fun saveAll(movies: List<Movie>): List<Movie> {
        return movieJpaRepository.saveAll(movies)
    }
    
    override fun save(movie: Movie): Movie {
        return movieJpaRepository.save(movie)
    }
    
    override fun findById(id: Long): Movie? {
        return movieJpaRepository.findById(id).orElse(null)
    }
}