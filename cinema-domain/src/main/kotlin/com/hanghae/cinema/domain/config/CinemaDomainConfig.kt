package com.hanghae.cinema.domain.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.domain.EntityScan

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = ["com.hanghae.cinema"])
@EnableJpaRepositories(basePackages = ["com.hanghae.cinema"])
@EntityScan(basePackages = ["com.hanghae.cinema"])
@EnableTransactionManagement
@SpringBootConfiguration
class CinemaDomainConfig 