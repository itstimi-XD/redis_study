package com.hanghae.cinema.infrastructure.persistence.genre

import com.hanghae.cinema.domain.genre.Genre
import com.hanghae.cinema.domain.genre.GenreRepository
import org.springframework.stereotype.Repository

@Repository
class GenreRepositoryImpl(
    private val genreJpaRepository: GenreJpaRepository
) : GenreRepository {
    
    override fun save(genre: Genre): Genre {
        return genreJpaRepository.save(genre)
    }
    
    override fun saveAll(genres: List<Genre>): List<Genre> {
        return genreJpaRepository.saveAll(genres)
    }
    
    override fun findAll(): List<Genre> {
        return genreJpaRepository.findAll()
    }
    
    override fun findById(id: Long): Genre? {
        return genreJpaRepository.findById(id).orElse(null)
    }
} 