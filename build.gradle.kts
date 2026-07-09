import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.spring") version "2.4.0"
    kotlin("plugin.jpa") version "2.4.0"
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.10.0"
}

group = "delivery"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.postgresql:postgresql")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    implementation("com.github.f4b6a3:uuid-creator:6.1.1")

    // Arrow.Either
    implementation("io.arrow-kt:arrow-core:2.2.2.1")

    // Logging
    implementation("org.zalando:logbook-spring-boot-starter:3.12.2")

    // Добавляем аннотации JetBrains для корректного вывода типов Spring-библиотек
    compileOnly("org.jetbrains:annotations:24.1.0")

    // gRPC + Protobuf (Kotlin)
    implementation("io.grpc:grpc-stub:1.76.0")
    implementation("io.grpc:grpc-protobuf:1.76.0")
    implementation("io.grpc:grpc-netty-shaded:1.76.0")
    implementation("com.google.protobuf:protobuf-kotlin:4.32.1")
    implementation("com.google.protobuf:protobuf-java-util:4.32.1")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testImplementation("io.kotest:kotest-assertions-arrow-jvm:6.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
    testImplementation("com.ninja-squad:springmockk:5.0.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}

tasks.register("printSrc") {
    doLast {
        file("src/main/kotlin").walkTopDown()
            .filter { it.isFile }
            .forEach { println(it.relativeTo(file("src/main/kotlin"))) }
    }
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:4.32.1" }
    plugins {
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.76.0" }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("kotlin")
            }
            task.plugins {
                id("grpc")
            }
        }
    }
}
