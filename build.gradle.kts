@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.panteleyev.jpackage.ImageType
import org.panteleyev.jpackage.JPackageTask
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Gradle plugins
plugins {
    kotlin("jvm") version "1.9.0"
    id("java")
    id("application")
    id("org.panteleyev.jpackageplugin") version "1.5.0"
}

// Project properties.
val projectId = property("project_id")
val projectName = property("project_name")
val projectVersion = property("project_version")
val appName = property("app_name")

// Dependency versions
val flatlafVersion = "3.2"
val chromeVersion = "116.0.19.1"

// Version information
val buildNr: Int = System.getenv("GITHUB_BUILD_NUMBER")?.toIntOrNull() ?: 0

val applicationVersion = "$projectVersion.$buildNr"
val applicationName = "$appName $projectVersion"

val packageVersion = applicationVersion
val viewVersion: String = applicationVersion
val buildDate: ZonedDateTime = ZonedDateTime.now()

group = "com.ultreon"
version = applicationVersion

// Repository and dependencies configuration
repositories {
    mavenCentral()
}

configurations {
    implementation {
        isCanBeResolved = true
    }
}

dependencies {
    implementation("com.formdev:flatlaf:$flatlafVersion")
    implementation("com.formdev:flatlaf-intellij-themes:$flatlafVersion")
    implementation("com.formdev:flatlaf-extras:$flatlafVersion")
    implementation("com.formdev:flatlaf-swingx:$flatlafVersion")
    implementation("commons-lang:commons-lang:2.6")
    implementation("org.drjekyll:fontchooser:2.5.2")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.miglayout:miglayout-swing:11.0")
    implementation("org.bidib.org.oxbow:swingbits:1.2.2")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    implementation("net.java.dev.jna:jna:5.13.0")
    testImplementation(kotlin("test"))
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("me.friwi:jcefmaven:$chromeVersion")
}

// Task configuration
tasks.jar {
    //noinspection GroovyAssignabilityCheck
    manifest {
        //noinspection GroovyAssignabilityCheck
        attributes(mapOf(
            Pair("Implementation-Title", "QBubbles"),
            Pair("Implementation-Vendor", "QTech Community"),
            Pair("Implementation-Version", "1.0-indev1"),
            Pair("Main-Class", "PreMain"),
            Pair("Agent-Class", "PreMain"),
            Pair("Premain-Class", "PreMain"),
            Pair("Multi-Release", "true")
        ))
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    inputs.dir("src/main/resources")

    inputs.property("project_id", projectId)
    inputs.property("project_name", projectName)
    inputs.property("app_name", appName)
    inputs.property("version", viewVersion)
    inputs.property("build_date", buildDate.format(DateTimeFormatter.RFC_1123_DATE_TIME))
    inputs.property("chrome_version", chromeVersion)

    filesMatching(listOf("docs/**.html", "product.json")) {
        expand(
            "project_id" to projectId,
            "project_name" to projectName,
            "app_name" to appName,
            "version" to viewVersion,
            "build_date" to buildDate.format(DateTimeFormatter.RFC_1123_DATE_TIME),
            "chrome_version" to chromeVersion
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.compileJava {
    targetCompatibility = "17"
    sourceCompatibility = "17"
}

mkdir("$projectDir/run/")

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jars")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("$buildDir/jars")
}

// Clear build/dist
task("cleanDist", Delete::class) {
    delete(fileTree("$buildDir/dist"))
    Files.deleteIfExists(Paths.get("$buildDir/dist"))
    group = "build"
}

// JPackage tasks configuration
tasks.jpackage {
    dependsOn("build", "copyDependencies", "copyJar", "cleanDist")

    group = "package"

    input  = "$buildDir/jars"
    destination = "$buildDir/dist"

    appName = project.property("app_name").toString()
    appVersion = project.version.toString()
    vendor = "Ultreon Team"
    copyright = "Copyright (c) 2022 Ultreon Team"
    runtimeImage = System.getProperty("java.home")

    mainJar = tasks.jar.get().archiveFileName.get()
    mainClass = "PreMain"

    destination = "$buildDir/dist"

    javaOptions = listOf("-Dfile.encoding=UTF-8")

    aboutUrl = "https://github.com/Ultreon/ultreon-browser"

    mac {
        icon = "icons/icon.icns"
        macPackageIdentifier = "com.ultreon.browser"
        macPackageName = "ultreon-browser"
        appVersion = packageVersion.replace(Regex("(\\d+\\.\\d+\\.\\d+).*"), "$1")
    }

    linux {
        icon = "icons/icon.png"
        linuxPackageName = "ultreon-browser"
        linuxDebMaintainer = "Ultreon Team"
        linuxRpmLicenseType = "Ultreon API License v1.1"
        linuxAppRelease = "2"
        linuxShortcut = true
        appVersion = project.version.toString()
    }

    windows {
        icon = "icons/icon.ico"
        winMenu = true
        winDirChooser = true
        winConsole = false
        winPerUserInstall = true
        winShortcutPrompt = true
        winShortcut = false
        winUpgradeUuid = "340c6842-b3bb-4173-bc87-c7c831cd1605"
        winMenuGroup = "Ultreon Team"
        appVersion = viewVersion.replace("+local", ".0").replace("+", ".")
        type = ImageType.MSI
    }
}

task("jpackageAlt", JPackageTask::class) {
    dependsOn("build", "copyDependencies", "copyJar", "cleanDist")

    group = "package"

    input  = "$buildDir/jars"
    destination = "$buildDir/dist"

    appName = project.property("app_name").toString()
    appVersion = project.version.toString()
    vendor = "Ultreon Team"
    copyright = "Copyright (c) 2022 Ultreon Team"
    runtimeImage = System.getProperty("java.home")

    mainJar = tasks.jar.get().archiveFileName.get()
    mainClass = "PreMain"

    destination = "$buildDir/dist"

    javaOptions = listOf("-Dfile.encoding=UTF-8")

    mac {
        aboutUrl = "https://github.com/Ultreon/ultreon-browser"
        licenseFile = "$projectDir/package/LICENSE.txt"
        icon = "icons/icon.icns"
        macPackageIdentifier = "com.ultreon.browser"
        macPackageName = "ultreon-browser"
        appVersion = packageVersion.replace(Regex("(\\d+\\.\\d+\\.\\d+).*"), "$1")
        type = ImageType.PKG
    }

    linux {
        icon = "icons/icon.png"
        appVersion = project.version.toString()
        type = ImageType.APP_IMAGE
    }

    windows {
        icon = "icons/icon.ico"
        appVersion = project.version.toString()
        type = ImageType.APP_IMAGE
        this.winConsole = true
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// Application configuration
application {
    mainClass.set("PreMain")
    this.applicationName = project.property("app_name").toString()
    this.applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dapp.version=${viewVersion}",
    )
}
