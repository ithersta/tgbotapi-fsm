plugins {
    kotlin("jvm") version "1.7.10"
}

group = "com.ithersta"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    implementation("dev.inmo:tgbotapi:3.2.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
