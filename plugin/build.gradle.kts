plugins {
  alias(libs.plugins.kotlin.jvm)
  `kotlin-dsl`
  id("com.jetbrains.toolbox.packaging")
  id("com.jetbrains.toolbox.install")
  `java-library`
}

group = "com.jetbrains.toolbox.sample"
version = "1.1.0"

kotlin {
  jvmToolchain(21)
}

dependencies {
  compileOnly(libs.bundles.toolbox.plugin.api)
  compileOnly(libs.coroutines.core)
}

// Known issue with kotlin 2.1.0 when using MutableStateFlow, please remove
// once https://youtrack.jetbrains.com/issue/KT-73951 is released and you upgrade version.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    freeCompilerArgs.add("-Xdisable-phases=ConstEvaluationLowering")
  }
}