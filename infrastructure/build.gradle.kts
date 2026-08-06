import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.protobuf)
}

dependencies {
    implementation(project(":common:types"))
    implementation(project(":application"))
    implementation(project(":domain"))

    // Spring
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.data.jdbc)

    implementation(libs.spring.kafka)

    // Logging
    implementation(libs.kotlin.logging.jvm)

    // Arrow.Either
    implementation(libs.arrow.core)

    implementation(libs.postgresql)

    // Flyway
    implementation(libs.spring.boot.starter.flyway)
    runtimeOnly(libs.flyway.database.postgresql)

    // gRPC + Protobuf (Kotlin)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.protobuf.kotlin)
    implementation(libs.protobuf.java.util)
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:4.35.1" }
    plugins {
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.83.1" }
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