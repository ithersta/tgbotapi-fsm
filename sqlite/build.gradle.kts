plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

version = "0.5.0"

dependencies {
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.tgbotapi.fsm)
    implementation("org.jetbrains.exposed", "exposed-core", "0.41.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.41.1")
    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.ithersta.tgbotapi"
            artifactId = "sqlite-persistence"
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


