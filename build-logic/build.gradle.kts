plugins {
  alias(libs.plugins.kotlin.jvm)
  `kotlin-dsl`
  `java-gradle-plugin`
}

kotlin {
  jvmToolchain(21)
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

dependencies {
  implementation(libs.plugin.structure)
  implementation(libs.jackson.kotlin)
}

gradlePlugin {
  plugins {
    create("toolboxPackaging") {
      id = "com.jetbrains.toolbox.packaging"
      implementationClass = "com.jetbrains.toolbox.buildlogic.ToolboxGenerateJsonExtension"
      displayName = "Generate Json Extension for Toolbox Plugin"
      description = "Registers a Zip task to package a JetBrains Toolbox plugin"
    }
    create("toolboxInstall") {
      id = "com.jetbrains.toolbox.install"
      implementationClass = "com.jetbrains.toolbox.buildlogic.InstallToolboxPlugin"
      displayName = "Install Toolbox Plugin"
      description = "Installs the plugin into the local Toolbox directory"
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    freeCompilerArgs.add("-Xdisable-phases=ConstEvaluationLowering")
  }
}