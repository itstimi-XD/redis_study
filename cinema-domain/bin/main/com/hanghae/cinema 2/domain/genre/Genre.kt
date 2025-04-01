package com.hanghae.cinema.domain.genre

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "genres")
class Genre(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    val name: String,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String = "system",
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var updatedBy: String = "system"
) 