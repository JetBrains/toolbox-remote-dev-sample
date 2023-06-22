import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfoRt
import java.nio.file.Path
import kotlin.io.path.div

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/tbx/gateway")
}

dependencies {
    implementation(libs.gateway.api)
    implementation(libs.slf4j)
    implementation(libs.bundles.serialization)
    implementation(libs.coroutines.core)
    implementation(libs.okhttp)
}

tasks.compileKotlin {
    kotlinOptions.freeCompilerArgs += listOf(
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
    )
}

val pluginId = "sample"

val assemblePlugin by tasks.registering(Jar::class) {
    archiveBaseName.set(pluginId)
    from(sourceSets.main.get().output)
}

val copyPlugin by tasks.creating(Copy::class.java) {
    dependsOn(assemblePlugin)

    val userHome = System.getProperty("user.home").let { Path.of(it) }
    val toolboxCachesDir = when {
        SystemInfoRt.isWindows -> System.getenv("LOCALAPPDATA")?.let { Path.of(it) } ?: (userHome / "AppData" / "Local")
        // currently this is the location that TBA uses on Linux
        SystemInfoRt.isLinux -> System.getenv("XDG_DATA_HOME")?.let { Path.of(it) } ?: (userHome / ".local" / "share")
        SystemInfoRt.isMac -> userHome / "Library" / "Caches"
        else -> error("Unknown os")
    } / "JetBrains" / "Toolbox"

    val pluginsDir = when {
        SystemInfoRt.isWindows -> toolboxCachesDir / "cache"
        SystemInfoRt.isLinux || SystemInfoRt.isMac -> toolboxCachesDir
        else -> error("Unknown os")
    } / "plugins"

    val targetDir = pluginsDir / pluginId
    val runtimeClasspath by configurations.getting

    from(assemblePlugin.get().outputs.files)

    val excludedJarPrefixes = listOf("gateway-api")
    val filteredClasspath = runtimeClasspath.filter { f ->
        !excludedJarPrefixes.any { p -> f.name.startsWith(p) }
    }

    from(filteredClasspath) {
        include("*.jar")
    }

    from("src/main/resources") {
        include("extensions.json")
        include("icon.svg")
    }

    into(targetDir)
}