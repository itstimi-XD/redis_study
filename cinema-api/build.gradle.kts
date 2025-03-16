plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":cinema-domain"))
    implementation(project(":cinema-application"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(libs.postgresql)
    
    // Swagger/OpenAPI
    implementation(libs.springdoc.openapi.starter)
    
    // TestContainers for both main and test
    implementation("org.testcontainers:postgresql:${libs.versions.test.containers.get()}")
    implementation("org.testcontainers:testcontainers:${libs.versions.test.containers.get()}")
    implementation("org.springframework.boot:spring-boot-testcontainers")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.bundles.testcontainers.postgresql)
}

tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
} 