plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

version = libs.versions.tgbotapi.fsm.get()

dependencies {
    api(libs.tgbotapi.fsm)
    api(libs.inmo.tgbotapi)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.ithersta.tgbotapi"
            artifactId = "tgbotapi-pagination"
            version = version

            from(components["java"])

            pom {
                name.set("Telegram Bot API Pagination DSL")
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}
