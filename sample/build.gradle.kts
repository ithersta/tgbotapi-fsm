plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.ithersta"
version = "unspecified"

dependencies {
    implementation(project(":lib"))
    implementation(project(":menu"))
    implementation(project(":pagination"))
    implementation(libs.inmo.tgbotapi)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}
