package com.hanghae.cinema.infrastructure.persistence.theater

import com.hanghae.cinema.domain.theater.Theater
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TheaterJpaRepository : JpaRepository<Theater, Long> 