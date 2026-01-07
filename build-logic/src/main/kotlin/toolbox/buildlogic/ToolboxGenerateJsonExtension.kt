package com.jetbrains.toolbox.buildlogic

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jetbrains.plugin.structure.toolbox.ToolboxMeta
import com.jetbrains.plugin.structure.toolbox.ToolboxPluginDescriptor
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

data class ExtensionJsonMeta(
  val name: String,
  val description: String,
  val vendor: String,
  val url: String?,
)

data class ExtensionJson(
  val id: String,
  val version: String,
  val meta: ExtensionJsonMeta,
)

abstract class GenerateExtensionJsonTask : DefaultTask() {
  @get:Input
  abstract val extensionId: Property<String>

  @get:Input
  abstract val extensionVersion: Property<String>

  @get:Input
  abstract val metaName: Property<String>

  @get:Input
  abstract val metaDescription: Property<String>

  @get:Input
  abstract val metaVendor: Property<String>

  @get:Input
  @get:org.gradle.api.tasks.Optional
  abstract val metaUrl: Property<String>

  @get:OutputFile
  abstract val destinationFile: RegularFileProperty

  @TaskAction
  fun run() {
    val descriptor = ToolboxPluginDescriptor(
      id = extensionId.get(),
      version = extensionVersion.get(),
      apiVersion = extensionVersion.get(),
      meta = ToolboxMeta(metaName.get(), metaDescription.get(), metaVendor.get(), metaUrl.orNull)
    )
    val content = jacksonObjectMapper().writeValueAsString(descriptor)
    destinationFile.get().asFile.apply {
      parentFile.mkdirs()
      writeText(content)
    }
  }
}

class ToolboxGenerateJsonExtension : Plugin<Project> {
  override fun apply(target: Project) {
    val extensionJsonFile = target.layout.buildDirectory.file("generated/extension.json")

    val gen = target.tasks.register("generateExtensionJson", GenerateExtensionJsonTask::class.java) {
      extensionId.set(target.group.toString())
      extensionVersion.set(target.version.toString())
      metaName.set("Toolbox Sample Plugin")
      metaDescription.set("Sample Plugin for JetBrains Toolbox")
      metaVendor.set("JetBrains")
      metaUrl.set("https://www.jetbrains.com/toolbox/")
      destinationFile.set(extensionJsonFile)
    }

    target.tasks.named("assemble").configure { dependsOn(gen) }
  }
}