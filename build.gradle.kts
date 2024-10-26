/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.BufferedReader
import java.io.InputStreamReader

// disable default versioning
version = "0.0.10"

// jvm target
val JVM = 17 // 1.8 for 8, 11 for 11

// target will be set to minecraft version by cli input parameter
var target = "1.21"
var kotlinVersion = "2.0.20"
fun executeCommand(command: String): String {
    val process = Runtime.getRuntime().exec(command)
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    return reader.readText().trim()
}

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.2"

    // maven() // no longer needed in gradle 7

    // Apply the application plugin to add support for building a CLI application.
    application

    `maven-publish`
}

repositories {
    gradlePluginPortal()
    
    maven { // paper
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven { // protocol lib
        url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
    }

    // fast block edit
    maven {
        url = uri("https://repo.repsy.io/mvn/tlm920/minecraft")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JVM))
    }
}

configurations {
    create("resolvableImplementation") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }
}

dependencies {
    // Align versions of all Kotlin components
    compileOnly(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    if (!project.hasProperty("no-kotlin")) { // shadow kotlin unless "no-kotlin" flag
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    } else {
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    // google json
    implementation("com.google.code.gson:gson:2.8.9")
    
    // protocol lib (nametag packets)
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

// TODO: resolve fast block edit build issues
//    when (target) {
//        "1.16" -> implementation("phonon.blockedit:fast-block-edit:1.16-SNAPSHOT")
//        "1.17" -> implementation("phonon.blockedit:fast-block-edit:1.17-SNAPSHOT")
//        "1.18" -> implementation("phonon.blockedit:fast-block-edit:1.18-SNAPSHOT")
//        "1.19" -> implementation("phonon.blockedit:fast-block-edit:1.19-SNAPSHOT")
//        "1.20" -> implementation("phonon.blockedit:fast-block-edit:1.20-SNAPSHOT")
//        "1.21" -> implementation("phonon.blockedit:fast-block-edit:1.21-SNAPSHOT")
//        else -> implementation("phonon.blockedit:fast-block-edit:1.21-SNAPSHOT") // fallback to latest
//    }

    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
}

application {
    // Define the main class for the application.
    mainClass.set("phonon.nodes.NodesPluginKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-Xallow-result-return-type")
}

tasks {
    named<ShadowJar>("shadowJar") {
        // verify valid target minecraft version
//        doFirst {
//            val supportedMinecraftVersions = setOf("1.12", "1.16", "1.18")
//            if ( !supportedMinecraftVersions.contains(target) ) {
//                throw Exception("Invalid Minecraft version! Supported versions are: 1.12, 1.16, 1.18")
//            }
//        }

        //relocate("phonon.blockedit", "nodes.lib.blockedit")
    }
}

tasks {
    processResources {
        val props = mapOf(
            "version" to project.version,
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    build {
        dependsOn(shadowJar)
    }
    
    test {
        testLogging.showStandardStreams = true
    }

    shadowJar {
        relocate("com.google", "nodes.shadow.gson")
        if (project.hasProperty("prod")) {
            archiveBaseName.set("${project.name}-${target}-${version}")
            minimize() // FOR PRODUCTION USE MINIMIZE
        }
        else {
            archiveFileName.set("${project.name}-${target}-${version}-DEV-${executeCommand("git rev-parse --short HEAD")}.jar")
        }
    }
}