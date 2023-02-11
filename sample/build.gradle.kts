plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.ksp)
}

group = "com.ithersta"
version = "unspecified"

dependencies {
    implementation(project(":lib"))
    implementation(project(":sqlite"))
    implementation(project(":commands"))
    implementation(project(":boot"))
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation(project(":test"))
    ksp(project(":boot-ksp"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
