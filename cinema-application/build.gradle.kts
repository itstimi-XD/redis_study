plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":cinema-domain"))
    implementation(project(":cinema-infrastructure"))
    
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Database
    implementation(libs.postgresql)
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.bundles.testcontainers.postgresql)
}

tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
} 