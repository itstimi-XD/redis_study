package com.hanghae.cinema.infrastructure.persistence.theater

import com.hanghae.cinema.domain.theater.Theater
import com.hanghae.cinema.domain.theater.TheaterRepository
import org.springframework.stereotype.Repository

@Repository
class TheaterRepositoryImpl(
    private val theaterJpaRepository: TheaterJpaRepository
) : TheaterRepository {
    
    override fun save(theater: Theater): Theater {
        return theaterJpaRepository.save(theater)
    }
    
    override fun saveAll(theaters: List<Theater>): List<Theater> {
        return theaterJpaRepository.saveAll(theaters)
    }
    
    override fun findAll(): List<Theater> {
        return theaterJpaRepository.findAll()
    }
    
    override fun findById(id: Long): Theater? {
        return theaterJpaRepository.findById(id).orElse(null)
    }
} 