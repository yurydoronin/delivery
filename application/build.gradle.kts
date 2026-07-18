plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(project(":common:types"))
    implementation(project(":domain"))

    implementation(libs.spring.boot.starter.data.jdbc)

    // Arrow.Either
    implementation(libs.arrow.core)

    implementation(libs.kotlin.reflect)

    implementation(libs.postgresql)

    // Flyway
    implementation("org.flywaydb:flyway-core:12.10.0")
    runtimeOnly(libs.flyway.database.postgresql)

    implementation(libs.uuid.creator)
}
