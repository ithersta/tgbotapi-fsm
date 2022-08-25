plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
    `maven-publish`
}

version = "0.8.0"

repositories {
    mavenCentral()
}

dependencies {
    api("dev.inmo:tgbotapi:3.1.1")
    api("io.insert-koin:koin-core:3.2.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.ithersta.tgbotapi"
            artifactId = "tgbotapi-fsm"
            version = version

            from(components["java"])

            pom {
                name.set("Telegram Bot API Finite State Machine DSL")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }

            repositories {
                maven {
                    url = uri("https://repo.repsy.io/mvn/ithersta/tgbotapi")
                    credentials {
                        username = properties["repsyUsername"].toString()
                        password = properties["repsyPassword"].toString()
                    }
                }
            }
        }
    }
}


