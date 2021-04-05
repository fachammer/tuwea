import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    application
}

group = "dev.achammer"
version = "0.2-SNAPSHOT"

object Libs {
    abstract class Lib(groupId: String, artifactId: String, version: String) {
        val dependencyPath = "$groupId:$artifactId:$version"
    }

    object KotlinCsv: Lib("com.github.doyaaaaaken", "kotlin-csv-jvm", "0.15.0")
    object Slf4jNop: Lib("org.slf4j", "slf4j-nop", "1.7.25")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(Libs.KotlinCsv.dependencyPath)
    implementation(Libs.Slf4jNop.dependencyPath)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("dev.achammer.tuwea.MainKt")
}