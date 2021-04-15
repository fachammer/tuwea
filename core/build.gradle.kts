import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.4.32"
}

group = "dev.achammer"
version = "0.2-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        browser {
            binaries.library()
        }
        nodejs {
            binaries.library()
        }
    }
    sourceSets {
        val commonMain by getting
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
