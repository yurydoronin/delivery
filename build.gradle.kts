import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.update.dependencies)
}

group = "delivery"
version = "1.0.0"

subprojects {
    repositories {
        mavenCentral()
    }
    // ленивая конфигурация подпроектов через менеджер плагинов
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(25)
        }
    }
}

// локальный запуск: ./gradlew dependencyUpdates --no-configuration-cache --no-parallel
tasks.withType<DependencyUpdatesTask>().configureEach {
    gradleReleaseChannel = "current" // только стабильные версии Gradle plugin

    rejectVersionIf {
        listOf("alpha", "beta", "rc", "snapshot")
            .any { candidate.version.contains(it, ignoreCase = true) }
    }
}
