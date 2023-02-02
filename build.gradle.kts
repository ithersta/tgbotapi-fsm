allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = uri("https://repo.repsy.io/mvn/ithersta/tgbotapi"))
    }
}

plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.7.20"
}
