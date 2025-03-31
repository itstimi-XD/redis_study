plugins {
    kotlin("plugin.jpa")
    kotlin("kapt")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

// Configure Spring Boot dependency management
dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation(project(":cinema-domain"))
    
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Database
    implementation("com.mysql:mysql-connector-j")
    
    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:mysql:${libs.versions.test.containers.get()}")
    testImplementation("org.testcontainers:junit-jupiter:${libs.versions.test.containers.get()}")
}

kotlin {
    jvmToolchain(17)
}

// QueryDSL 설정
sourceSets {
    main {
        kotlin {
            srcDirs(layout.buildDirectory.dir("generated/source/kapt/main"))
        }
    }
}

tasks.jar {
    enabled = true
}

// Q클래스 생성을 위한 kapt 설정
kapt {
    arguments {
        arg("querydsl.packageName", "com.hanghae.cinema.infrastructure.persistence")
    }
}