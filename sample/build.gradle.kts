plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.ithersta"
version = "unspecified"

dependencies {
    implementation(project(":lib"))
    implementation(project(":sqlite"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}
