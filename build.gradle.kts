plugins {
    kotlin("jvm") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("org.slf4j:slf4j-nop:2.0.9")
    implementation("org.json:json:20231013")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}