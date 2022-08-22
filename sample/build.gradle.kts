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
    implementation("dev.inmo:tgbotapi:3.1.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
