plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":cinema-domain"))
    implementation(project(":cinema-infrastructure"))
    
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.19.6")
    testImplementation("org.testcontainers:junit-jupiter:1.19.6")
    testImplementation("org.testcontainers:mysql:1.19.6")
    testImplementation("com.mysql:mysql-connector-j")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}

tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
} 