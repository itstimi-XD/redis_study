plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // JPA
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
} 