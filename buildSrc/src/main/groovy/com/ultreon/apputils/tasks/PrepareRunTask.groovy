package com.ultreon.apputils.tasks

import com.ultreon.apputils.AppUtilsExt
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class PrepareRunTask extends DefaultTask {
    PrepareRunTask() {
        dependsOn("genIdeaRuns")
        dependsOn("clearQuiltCache")
        group = "appUtils"
    }

    @TaskAction
    void createRun() {
        def runDirectory = project.rootProject.extensions.getByType(AppUtilsExt).runDirectory

        if (runDirectory.isFile()) {
            println("ERROR: Run directory is obstructed by a file: $runDirectory")
            return;
        }

        try {
            if (!runDirectory.exists() && !runDirectory.mkdirs()) {
                println("ERROR: Failed to create run directory")
            }
        } catch (exception) {
            throw new GradleException("Failed to create run directory: ", exception)
        }
    }
}
