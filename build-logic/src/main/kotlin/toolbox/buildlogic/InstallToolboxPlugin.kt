package com.jetbrains.toolbox.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.nio.file.Path
import java.util.*
import kotlin.io.path.div

class InstallToolboxPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val installTask = target.tasks.register("installPlugin", InstallTask::class.java) {
      extensionId.set(target.group.toString())
      extensionJsonFile.set(target.layout.buildDirectory.file("generated/extension.json"))
    }
    installTask.configure { dependsOn(target.tasks.named("assemble")) }
  }

  @DisableCachingByDefault(because = "Installs files into user directory")
  abstract class InstallTask : DefaultTask() {
    @get:Input
    abstract val extensionId: Property<String>

    @get:InputFile
    abstract val extensionJsonFile: RegularFileProperty

    @TaskAction
    fun install() {
      println("Installing Toolbox plugin...")
      project.sync {
        val userHome = System.getProperty("user.home").let { Path.of(it) }
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        val toolboxCachesDir = when {
          os.contains("win") -> System.getenv("LOCALAPPDATA")?.let { Path.of(it) } ?: (userHome / "AppData" / "Local")
          os.contains("linux") -> System.getenv("XDG_DATA_HOME")?.let { Path.of(it) } ?: (userHome / ".local" / "share")
          os.contains("mac") -> userHome / "Library" / "Caches"
          else -> error("Unknown os: $os")
        } / "JetBrains" / "Toolbox"

        val pluginsDir = when {
          os.contains("win") -> toolboxCachesDir / "cache"
          os.contains("linux") || os.contains("mac") -> toolboxCachesDir
          else -> error("Unknown os: $os")
        } / "plugins"

        val targetDir = pluginsDir / extensionId.get()

        // Copy jar task output and the generated JSON
        from(project.tasks.getByName("jar"))
        from(extensionJsonFile)

        // Copy selected resources
        from("src/main/resources") {
          include("dependencies.json")
          include("icon.svg")
        }
        into(targetDir)
      }
    }
  }
}
