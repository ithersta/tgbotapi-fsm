plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

version = libs.versions.tgbotapi.fsm.get()

dependencies {
    implementation(libs.kotlinx.serialization.protobuf)
    api(libs.inmo.tgbotapi)
    api(libs.koin.core)
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
                        username = rootProject.properties["repsyUsername"].toString()
                        password = rootProject.properties["repsyPassword"].toString()
                    }
                }
            }
        }
    }
}


