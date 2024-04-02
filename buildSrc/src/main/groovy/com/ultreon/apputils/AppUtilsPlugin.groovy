package com.ultreon.apputils


import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.stream.JsonWriter
import com.ultreon.apputils.tasks.MetadataTask
import com.ultreon.apputils.tasks.PrepareRunTask
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

@SuppressWarnings('unused')
class AppUtilsPlugin implements Plugin<Project> {
    static AppUtilsExt extension

    AppUtilsPlugin() {

    }

    @Override
    void apply(Project project) {
        if (project != project.rootProject) return

        extension = project.extensions.create("appUtils", AppUtilsExt.class)
        extension.runDirectory = project.file("run")

        project.configurations.register("pack") {
            it.canBeResolved = true
            it.canBeConsumed = true
        }

        project.afterEvaluate {
            evaluateAfter(project)
        }

        project.subprojects.collect { Project subproject ->
            handleSubproject(subproject)
        }

        project.tasks.register("prepareRun", PrepareRunTask.class)
    }

    private static void handleSubproject(Project subproject) {
        subproject.extensions.create("projectConfig", ProjectConfigExt)

        subproject.beforeEvaluate {
            def projectConfig = subproject.extensions.getByType(ProjectConfigExt)
            def jarTasks = subproject.tasks.withType(Jar).toList()
            jarTasks.collect { Jar jar ->
                jar.archiveBaseName.set("$subproject.name")
                jar.archiveVersion.set(extension.projectVersion)
                jar.archiveFileName.set("$subproject.name-${extension.projectVersion}.jar")

                for (Task dependsTask : projectConfig.jarDependTasks.get()) {
                    dependsTask.dependsOn(jar)
                }
            }

            subproject.properties.put "app_name", extension.projectName
            subproject.version = extension.projectVersion
            subproject.group = extension.projectGroup
        }

        subproject.afterEvaluate {
            def platform = subproject.extensions.getByType(ProjectConfigExt)
            if (platform == null || platform.type == null) {
//                throw new GradleException("Platform not set for project ${subproject.path}")
            }
        }
    }

    private static void evaluateAfter(Project project) {
        if (!extension.production) {
            println("WARNING: App $extension.projectName is in developer mode.")
        }

        if (extension.javaVersion == -1) {
            throw new GradleException("Java Version is not set.")
        }
        if (extension.packageProject == null) {
            throw new GradleException("Project to package is not set.")
        }
        if (extension.mainClass == null) {
            throw new GradleException("Main class is not set.")
        }

        if (extension.packageProject != project.rootProject) {
            extension.packageProject.with { packProject ->
                packProject.configurations.register("pack") {
                    it.canBeResolved = true
                    it.canBeConsumed = true
                }
            }
        }

        project.subprojects {
            project.version = extension.projectVersion
        }

        TaskProvider<MetadataTask> metadataTask = project.tasks.register("metadata", MetadataTask.class)
        for (Project subproject : project.subprojects) {
            subproject.tasks.withType(Jar).configureEach { Jar jar ->
                metadataTask.configure {
                    it.dependsOn(jar)
                }
            }
        }

        project.rootProject.tasks.register("pack", Zip) { Zip zip ->
            Project packProject = extension.packageProject
            zip.dependsOn metadataTask
            zip.group = "appUtils"

            zip.archiveFileName.set("package.zip")

            def json = new JsonObject()
            def classpathJson = new JsonArray()

            packProject.configurations.pack.with { Configuration conf ->
                List<Dep> dependencies = getDependencies(conf)
                dependencies.collect { Dep dep ->
                    File file = dep.file
                    println("Adding \"$file.name\" to classpath")
                    String name = getLibraryName(dep, file, zip)
                    classpathJson.add "libraries/" + dep.group.replaceAll("\\.", "/") + "/" + dep.name + "/" + name
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

                    return null
                }
            }
            doFirst {
                createAppJson(json, classpathJson, packProject)
            }

            zip.from(metadataTask.get().metadataFile)
            zip.from("$packProject.projectDir/build/app.json")
            zip.from(project.tasks.jar.outputs, new Action<CopySpec>() {
                @Override
                void execute(CopySpec copySpec) {
                    copySpec.rename { extension.projectVersion + ".jar" }
                }
            })

            println metadataTask.get().metadataFile

            zip.destinationDirectory.set(project.file("$packProject.projectDir/build/dist"))
            zip.archiveBaseName.set("package")

            packProject.delete(zip.archiveFile)
        }
    }

    private static List<Dep> getDependencies(Configuration conf) {
        List<Dep> dependencies = []
        if (conf.isCanBeResolved()) {
            conf.getResolvedConfiguration().getResolvedArtifacts().each {
                at ->
                    def dep = at.getModuleVersion().getId()
                    dependencies.add(new Dep(dep.group, dep.name, dep.version, at.extension, at.classifier, at.file))
            }
        } else {
            throw new GradleException("Pack config can't be resolved!")
        }
        dependencies
    }

    private static void createAppJson(JsonObject json, JsonArray classpathJson, Project packProject) {
        json.add("classpath", classpathJson)

        def sdkJson = new JsonObject()
        sdkJson.addProperty("version", ">= ${packProject.tasks.named("compileJava", JavaCompile).get().targetCompatibility}")
        sdkJson.addProperty("type", "JavaJDK")
        sdkJson.addProperty("sdk_type", "java")
        json.add("sdk", sdkJson)
        json.addProperty("main-class", extension.mainClass)
        json.addProperty("app", extension.projectId)

        def gson = new GsonBuilder().create()
        def writer = new JsonWriter(new FileWriter(packProject.file("$packProject.projectDir/build/app.json")))
        gson.toJson json, writer
        writer.flush()
        writer.close()
    }

    private static String getLibraryName(Dep dep, File file, Zip zip) {
        String name
        if (dep.classifier == null || dep.classifier == "null") {
            name = dep.name + "-" + dep.version + "." + dep.extension
        } else {
            name = dep.name + "-" + dep.version + "-" + dep.classifier + "." + dep.extension
        }
        {
            def dest = "libraries/" + dep.group.replaceAll("\\.", "/") + "/" + dep.name
            println "Adding \"$file.name\" to \"$dest\""

            zip.from file, new Action<CopySpec>() {
                @Override
                void execute(CopySpec spec) {
                    spec.into(dest)
                }
            }
        }
        name
    }
}
