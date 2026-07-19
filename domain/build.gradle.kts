plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":common:types"))

    implementation(libs.uuid.creator)

    // Arrow.Either
    implementation(libs.arrow.core)
}
