import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.protobuf)
}

dependencies {
    implementation(project(":application"))
    implementation(project(":common:types"))

    // Spring
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.quartz)

    implementation(libs.spring.kafka)

    implementation(libs.jackson.kotlin)

    // Arrow.Either
    implementation(libs.arrow.core)

    // gRPC + Protobuf (Kotlin)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.protobuf.kotlin)
    implementation(libs.protobuf.java.util)
}

tasks.register("printSrc") {
    description = "Вывод путей Kotlin исходников"

    val kotlinSrc = kotlin.sourceSets.main.map { it.kotlin }

    inputs.files(kotlinSrc)

    doLast {
        kotlinSrc.get().asFileTree.visit {
            if (!isDirectory) println(relativePath)
        }
    }
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
