plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.ithersta"
version = "unspecified"

dependencies {
    implementation(project(":lib"))
    implementation(project(":sqlite"))
    implementation(project(":commands"))
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation(project(":test"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}
