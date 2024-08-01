@file:Suppress("SpellCheckingInspection")

import com.ultreon.apputils.AppUtilsExt
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.kotlin.dsl.support.zipTo
import org.gradle.tooling.model.GradleTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.panteleyev.jpackage.ImageType
import org.panteleyev.jpackage.JPackageTask
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Gradle plugins
plugins {
    kotlin("jvm") version "1.9.22"
    id("java")
    id("application")
    id("org.panteleyev.jpackageplugin") version "1.+"
}

apply(plugin = "apputils")

// Project properties.
val projectId = property("project_id")
val projectName = property("project_name")
val projectVersion = property("project_version")
val appName = property("app_name")

// Dependency versions
val flatlafVersion = "3.4"
val chromeVersion = "126.2.0"

// Version information
val buildNr: Int = System.getenv("GITHUB_BUILD_NUMBER")?.toIntOrNull() ?: 0

val applicationVersion = "$projectVersion.$buildNr"
val applicationName = "$appName $projectVersion"

val packageVersion = applicationVersion
val viewVersion: String = applicationVersion
val buildDate: ZonedDateTime = ZonedDateTime.now()

extensions.getByType<AppUtilsExt>().apply {
    projectVersion = applicationVersion
    projectGroup = project.group.toString()
    projectName = "Ultreon Browser"
    projectId = project.name

    mainClass = "PreMain"

    packageProject = project

    javaVersion = 8
}

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
    testImplementation(kotlin("test"))

    implementation("org.burningwave:core:12.+")

    implementation("com.formdev:flatlaf:$flatlafVersion")!!
    implementation("com.formdev:flatlaf-intellij-themes:$flatlafVersion")!!
    implementation("com.formdev:flatlaf-extras:$flatlafVersion")!!
    implementation("com.formdev:flatlaf-swingx:$flatlafVersion")!!
    implementation("commons-lang:commons-lang:2.6")!!
    implementation("org.drjekyll:fontchooser:2.5.2")!!
    implementation("com.google.code.gson:gson:2.10")!!
    implementation("com.miglayout:miglayout-swing:11.0")!!
    implementation("org.bidib.org.oxbow:swingbits:1.2.2")!!
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")!!
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")!!
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")!!
    implementation("net.java.dev.jna:jna:5.13.0")!!
    implementation("org.jetbrains:annotations:23.0.0")!!
    implementation("me.friwi:jcefmaven:$chromeVersion")!!
    configurations["pack"]("com.formdev:flatlaf:$flatlafVersion")
    configurations["pack"]("com.formdev:flatlaf-intellij-themes:$flatlafVersion")
    configurations["pack"]("com.formdev:flatlaf-extras:$flatlafVersion")
    configurations["pack"]("com.formdev:flatlaf-swingx:$flatlafVersion")
    configurations["pack"]("commons-lang:commons-lang:2.6")
    configurations["pack"]("org.drjekyll:fontchooser:2.5.2")
    configurations["pack"]("com.google.code.gson:gson:2.10")
    configurations["pack"]("com.miglayout:miglayout-swing:11.0")
    configurations["pack"]("org.bidib.org.oxbow:swingbits:1.2.2")
    configurations["pack"]("org.apache.logging.log4j:log4j-core:2.20.0")
    configurations["pack"]("org.apache.logging.log4j:log4j-api:2.20.0")
    configurations["pack"]("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    configurations["pack"]("net.java.dev.jna:jna:5.13.0")
    configurations["pack"]("org.jetbrains:annotations:23.0.0")
    configurations["pack"]("me.friwi:jcefmaven:$chromeVersion")
}

// Task configuration
tasks.jar {
    //noinspection GroovyAssignabilityCheck
    manifest {
        //noinspection GroovyAssignabilityCheck
        attributes(
            mapOf(
                Pair("Implementation-Title", "Ultreon Browser"),
                Pair("Implementation-Vendor", "Ultreon Team"),
                Pair("Implementation-Version", "$projectVersion"),
                Pair("Main-Class", "PreMain"),
                Pair("Agent-Class", "PreMain"),
                Pair("Premain-Class", "PreMain"),
                Pair("Multi-Release", "true")
            )
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Zip>("packageZip") {
    group = "packaging"
    description = "Pack the application"

    dependsOn(":jar")

    doFirst {
        copy {
            from("src/meta/app.json")
            into("$projectDir/build/temp/")

            filesMatching("*.json") {
                filter<ReplaceTokens>(
                    "tokens" to mapOf(
                        "version" to project.version,
                        "last_modified" to buildDate.format(DateTimeFormatter.ISO_INSTANT)
                    )
                )
            }
        }

        copy {
            from("src/meta/appmeta.json")
            into("$projectDir/build/temp/")
        }

        copy {
            from(tasks.jar.get().outputs)
            into("$projectDir/build/temp/")
        }
    }

    from(files("$projectDir/build/temp/*.jar")) {
        rename { "${project.version}.jar" }
    }

    from(file("$projectDir/LICENSE"))
    from(files("$projectDir/build/temp/*.json"))

    archiveFileName.set("package.zip")

    doLast() {
        delete("$projectDir/build/temp/")
        copy {
            from(outputs)
            into("$projectDir/build/dist/")
        }
    }
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
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileJava {
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}

mkdir("$projectDir/run/")

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$projectDir/build/jars")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("$projectDir/build/jars")
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
