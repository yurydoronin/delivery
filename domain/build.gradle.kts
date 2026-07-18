plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":common:types"))

    implementation("com.github.f4b6a3:uuid-creator:6.1.1")

    // Arrow.Either
    implementation("io.arrow-kt:arrow-core:2.2.2.1")
}
