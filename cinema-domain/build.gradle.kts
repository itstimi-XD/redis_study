plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

// Configure Spring Boot dependency management
dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

// Add explicit repositories section
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // JPA
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Spring annotations
    implementation("org.springframework:spring-context")
    
    // Database
    implementation("com.mysql:mysql-connector-j")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("com.h2database:h2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers:1.19.6")
    testImplementation("org.testcontainers:junit-jupiter:1.19.6")
    testImplementation("org.testcontainers:mysql:1.19.6")
    
    // Infrastructure module for testing
    testImplementation(project(":cinema-infrastructure"))
} 