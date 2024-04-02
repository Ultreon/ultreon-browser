package com.ultreon.apputils

import org.gradle.api.Project
import java.time.Instant

class AppUtilsExt {
    String projectName
    String projectVersion = "dev"
    String projectGroup = "com.example"
    String projectId = "example-project"
    Project packageProject
    int javaVersion = -1
    boolean production = false
    final buildDate = Instant.now()
    File runDirectory
    String mainClass
}
