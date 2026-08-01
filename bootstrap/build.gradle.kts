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
    implementation(project(":infrastructure"))

    implementation(libs.spring.boot.starter)

    // Logging
    implementation(libs.logbook.spring.boot.starter)
}

springBoot {
    mainClass.set("delivery.DeliveryApplicationKt")
}


/*
    Плагин io.spring.dependency-management по умолчанию переопределяет любые явно указанные версии в блоке dependencies,
    если они описаны в его BOM-файлах.
    Принудительное переопределение версий внутри Spring BOM
*/
dependencyManagement {
    imports {
        mavenBom("io.grpc:grpc-bom:1.83.1")
    }
    dependencies {
        dependency("com.google.protobuf:protobuf-java:4.35.1")
        dependency("com.google.protobuf:protobuf-java-util:4.35.1")
        dependency("com.google.protobuf:protobuf-kotlin:4.35.1")
    }
}