package com.jetbrains.toolbox.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.intellij.pluginRepository.PluginRepositoryFactory
import org.jetbrains.intellij.pluginRepository.model.ProductFamily

/**
 * Gradle plugin that packages and publishes a JetBrains Toolbox plugin to the
 * [JetBrains Marketplace](https://plugins.jetbrains.com).
 */
class PublishToolboxPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val packageTask = target.tasks.register("packagePlugin", Zip::class.java) {
      dependsOn(target.tasks.named("assemble"))
      from(target.layout.buildDirectory.file("generated/extension.json")) {
        into("${target.group}")
      }
      from(target.file("src/main/resources")) {
        include("dependencies.json")
        include("icon.svg")
        into("${target.group}")
      }
      from(target.tasks.named("jar")) {
        into("${target.group}/lib")
      }
    }

    target.tasks.register("publishPlugin", PublishTask::class.java) {
      dependsOn(packageTask)
      extensionId.set(target.group.toString())
      pluginZipFile.set(packageTask.flatMap { it.archiveFile })
    }
  }

  /**
   * Task that uploads the packaged plugin ZIP to the JetBrains Marketplace.
   *
   * Requires the `JETBRAINS_MARKETPLACE_PUBLISH_TOKEN` environment variable to be set.
   * The token can be generated from your JetBrains Marketplace account.
   */
  @DisableCachingByDefault(because = "Publishes plugin to JetBrains Marketplace")
  abstract class PublishTask : DefaultTask() {
    @get:Input
    abstract val extensionId: Property<String>

    /** Path to the plugin ZIP archive produced by the `packagePlugin` task. */
    @get:InputFile
    abstract val pluginZipFile: RegularFileProperty

    @TaskAction
    fun publish() {
      val token = System.getenv("JETBRAINS_MARKETPLACE_PUBLISH_TOKEN")
      if (token.isNullOrBlank()) {
        error(
          "Environment variable `JETBRAINS_MARKETPLACE_PUBLISH_TOKEN` is not set. " +
            "Please obtain a token from https://plugins.jetbrains.com and set it."
        )
      }

      println("Publishing plugin ${extensionId.get()} to JetBrains Marketplace...")
      println("Token prefix: ${token.take(5)}*****")

      val instance = PluginRepositoryFactory.create(
        "https://plugins.jetbrains.com",
        token
      )

      instance.uploader.uploadUpdateByXmlIdAndFamily(
        extensionId.get(),
        ProductFamily.TOOLBOX,
        pluginZipFile.get().asFile,
        null, // channel – not yet supported for Toolbox plugins.
        "Bug fixes and improvements", // please make sure to update version notes here.
        false
      )

      println("Plugin published successfully!")
    }
  }
}
