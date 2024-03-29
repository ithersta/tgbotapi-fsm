plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
}

version = "0.3.0"

dependencies {
    implementation(libs.tgbotapi.fsm)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.ithersta.tgbotapi"
            artifactId = "commands"
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


