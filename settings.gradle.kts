rootProject.name = "toolbox-gateway-sample"

include("plugin")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/tbx/toolbox-api")
  }
}

dependencyResolutionManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/tbx/toolbox-api")
  }
}

