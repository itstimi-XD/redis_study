package com.hanghae.cinema.domain.movie

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MovieService(
    private val movieRepository: MovieRepository
) {
    @Transactional(readOnly = true)
    fun findNowPlayingMovies(title: String? = null, genre: String? = null): List<Movie> {
        // 검색 조건이 없을 경우 기존 메서드 사용
        if (title.isNullOrBlank() && genre.isNullOrBlank()) {
            return movieRepository.findByReleaseDateLessThanEqualOrderByReleaseDateDesc(LocalDate.now())
        }

        // 검색 조건이 있는 경우 커스텀 메서드 사용
        return movieRepository.findNowPlayingMoviesWithFilters(title, genre)
    }
} 