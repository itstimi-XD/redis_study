package com.hanghae.cinema.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@Tag(name = "Hello", description = "Hello API")
class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "Hello 메시지 조회", description = "간단한 Hello 메시지를 반환합니다.")
    fun hello(): Map<String, String> {
        return mapOf("message" to "Hello, Cinema API!")
    }
} 