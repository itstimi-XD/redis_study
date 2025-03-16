pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

rootProject.name = "hanghae-cinema"

// 멀티모듈 설정
include(
    "cinema-api",
    "cinema-application",
    "cinema-domain",
    "cinema-infrastructure"
)