plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":api"))
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":common:types"))
    implementation(project(":infrastructure"))

    implementation(libs.spring.boot.starter)

    // Logging
    implementation(libs.logbook.spring.boot.starter)
}

springBoot {
    mainClass.set("delivery.DeliveryApplicationKt")
}
