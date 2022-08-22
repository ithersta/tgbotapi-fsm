plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
}

version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.inmo:tgbotapi:3.1.1")
}
