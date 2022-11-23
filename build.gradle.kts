@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "1.7.10"
    id("java")
    id("org.panteleyev.jpackageplugin") version "1.5.0"
}

// Project properties.
val projectId = property("project_id")
val projectName = property("project_name")
val projectVersion = property("project_version")
val appName = property("app_name")

// Dependency versions
val flatlafVersion = "2.6"

group = "com.ultreon"
version = "${projectVersion}-${if (System.getenv("GITHUB_BUILD_NUMBER") == null) "local" else System.getenv("GITHUB_BUILD_NUMBER")}"

val packageVersion = (version as String).replace("+local", ".0").replace("+", ".")

fun getViewVersion(): Any {
    return "${projectVersion}+${if (System.getenv("GITHUB_BUILD_NUMBER") == null) "local" else System.getenv("GITHUB_BUILD_NUMBER")}"
}

val buildDate: ZonedDateTime = ZonedDateTime.now()

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
    testImplementation(kotlin("test"))
    implementation("org.bidib.org.oxbow:swingbits:1.2.2")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("me.friwi:jcefmaven:107.1.9")
}

tasks.jar {
    for (it in configurations.implementation.get().files) {
        if (!it.path.startsWith(projectDir.path)) {
            if (it.isDirectory) {
                from(it)
            } else {
                from(zipTree(it))
            }
        }
    }

    exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF")

    //noinspection GroovyAssignabilityCheck
    manifest {
        //noinspection GroovyAssignabilityCheck
        attributes(mapOf(
            Pair("Implementation-Title", "QBubbles"),
            Pair("Implementation-Vendor", "QTech Community"),
            Pair("Implementation-Version", "1.0-indev1"),
            Pair("Main-Class", "MainKt"),
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
    inputs.property("version", getViewVersion())
    inputs.property("build_date", buildDate.format(DateTimeFormatter.RFC_1123_DATE_TIME))

    filesMatching(listOf("docs/**.html", "product.json")) {
        expand(
            "project_id" to projectId,
            "project_name" to projectName,
            "app_name" to appName,
            "version" to getViewVersion(),
            "build_date" to buildDate.format(DateTimeFormatter.RFC_1123_DATE_TIME)
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileJava {
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}

mkdir("$projectDir/run/")

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jars")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("$buildDir/jars")
}

tasks.jpackage {
    dependsOn("build", "copyDependencies", "copyJar")

    input  = "$buildDir/jars"
    destination = "$buildDir/dist"

    appName = "Notepad Improved 2"
    appVersion = project.version.toString()
    vendor = "Ultreon Team"
    copyright = "Copyright (c) 2022 Ultreon Team"
    runtimeImage = System.getProperty("java.home")

    mainJar = tasks.jar.get().archiveFileName.get()
    mainClass = "com.ultreon.notepadimproved.MainKt"

    destination = "$buildDir/dist"

    licenseFile = "$projectDir/package/LICENSE.txt"
    aboutUrl = "https://github.com/Ultreon/notepad-improved-2"

    javaOptions = listOf("-Dfile.encoding=UTF-8")

    mac {
        icon = "icons/icons.icns"
        macPackageIdentifier = "com.ultreon.notepadimproved"
        macPackageName = "notepad-improved"
        appVersion = packageVersion.replace(Regex("(\\d+\\.\\d+\\.\\d+).*"), "$1")
    }

    linux {
        icon = "icons/icons.png"
        linuxPackageName = "notepad-improved"
        linuxDebMaintainer = "Ultreon Team"
        linuxRpmLicenseType = "Ultreon API License v1.1"
        linuxAppRelease = "2"
        linuxShortcut = true
        appVersion = project.version.toString()
    }

    windows {
        icon = "icons/icons.ico"
        winMenu = true
        winDirChooser = true
        winConsole = false
        winPerUserInstall = true
        winShortcutPrompt = true
        winShortcut = false
        winUpgradeUuid = "0dd76e9b-dd95-495d-876e-9da69c86329c"
        winMenuGroup = "Ultreon Team"
        appVersion = (version as String).replace("+local", ".0").replace("+", ".")
    }
}
