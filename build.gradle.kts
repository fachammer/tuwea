import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.4.32"
    application
}

group = "dev.achammer"
version = "0.2-SNAPSHOT"

object Libs {
    abstract class Lib(groupId: String, artifactId: String, version: String) {
        val dependencyPath = "$groupId:$artifactId:$version"
    }

    object KotlinCsv : Lib("com.github.doyaaaaaken", "kotlin-csv-jvm", "0.15.0")
    object Slf4jNop : Lib("org.slf4j", "slf4j-nop", "1.7.25")
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser {
            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting
        val jvmMain by getting {
            dependencies {
                implementation(Libs.KotlinCsv.dependencyPath)
                implementation(Libs.Slf4jNop.dependencyPath)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.21")
                implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.21")
                implementation("org.jetbrains:kotlin-styled:5.2.1-pre.148-kotlin-1.4.21")
                implementation(npm("styled-components", "~5.2.1"))
                implementation(npm("react", "17.0.1"))
                implementation(npm("react-dom", "17.0.1"))
            }
        }
    }
}

repositories {
    mavenCentral()
    jcenter()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("dev.achammer.tuwea.MainKt")
}

distributions {
    main {
        contents {
            from("README.md")
            from("LICENSE")
            from("$buildDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}