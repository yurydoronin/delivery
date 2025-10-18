import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    kotlin("plugin.jpa") version "2.2.20"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.5"
}

group = "atm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.postgresql:postgresql")

    // Arrow.Either
    implementation("io.arrow-kt:arrow-core:2.1.2")

    // Logging
    implementation("org.zalando:logbook-spring-boot-starter:3.12.2")

    // gRPC + Protobuf (Kotlin)
    implementation("io.grpc:grpc-stub:1.76.0")
    implementation("io.grpc:grpc-protobuf:1.76.0")
    implementation("io.grpc:grpc-netty-shaded:1.76.0")
    implementation("com.google.protobuf:protobuf-kotlin:4.32.1")
    // (опционально для отладки)
    implementation("com.google.protobuf:protobuf-java-util:4.32.1")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testImplementation("io.kotest:kotest-assertions-arrow-jvm:6.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
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
