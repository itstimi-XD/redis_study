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
}

tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
} 