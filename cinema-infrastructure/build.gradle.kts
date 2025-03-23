plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":cinema-domain"))
    
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly(libs.postgresql)
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.bundles.testcontainers.postgresql)

    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
}

tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

// Q클래스 생성을 위한 kapt 설정
kapt {
    arguments {
        arg("querydsl.packageName", "com.hanghae.cinema.infrastructure.persistence")
    }
}