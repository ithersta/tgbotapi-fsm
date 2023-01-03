allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = uri("https://repo.repsy.io/mvn/ithersta/tgbotapi"))
    }
}

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
}
