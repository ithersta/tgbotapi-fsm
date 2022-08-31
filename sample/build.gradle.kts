plugins {
    kotlin("jvm")
}

group = "com.ithersta"
version = "unspecified"

dependencies {
    implementation(project(":lib"))
    implementation(project(":menu"))
    implementation(libs.inmo.tgbotapi)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
