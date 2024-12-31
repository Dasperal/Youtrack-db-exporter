plugins {
    java
    kotlin("jvm") version "2.1.0"
    application
    idea
}

group = "io.github.dasperal"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.xodus:xodus-openAPI:2.0.1")
    implementation("org.jetbrains.xodus:xodus-entity-store:2.0.1")
    implementation("org.jetbrains.xodus:xodus-environment:2.0.1")
    implementation(kotlin("stdlib-jdk8"))
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

application {
    mainClass = "io.github.dasperal.Main"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "io.github.dasperal.Main"
    }
}