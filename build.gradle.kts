import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jk1.license.filter.ExcludeTransitiveDependenciesFilter
import com.github.jk1.license.render.JsonReportRenderer
import com.jetbrains.plugin.structure.toolbox.ToolboxMeta
import com.jetbrains.plugin.structure.toolbox.ToolboxPluginDescriptor
import org.jetbrains.intellij.pluginRepository.PluginRepositoryFactory
import org.jetbrains.intellij.pluginRepository.model.LicenseUrl
import org.jetbrains.intellij.pluginRepository.model.ProductFamily
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfoRt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    `java-library`
    alias(libs.plugins.dependency.license.report)
    alias(libs.plugins.gradle.wrapper)
    alias(libs.plugins.gettext)
}

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/tbx/toolbox-api")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.marketplace.client)
        classpath(libs.plugin.structure)
    }
}

jvmWrapper {
    unixJvmInstallDir = "jvm"
    winJvmInstallDir = "jvm"
    linuxAarch64JvmUrl = "https://cache-redirector.jetbrains.com/intellij-jbr/jbr_jcef-21.0.5-linux-aarch64-b631.28.tar.gz"
    linuxX64JvmUrl = "https://cache-redirector.jetbrains.com/intellij-jbr/jbr_jcef-21.0.5-linux-x64-b631.28.tar.gz"
    macAarch64JvmUrl = "https://cache-redirector.jetbrains.com/intellij-jbr/jbr_jcef-21.0.5-osx-aarch64-b631.28.tar.gz"
    macX64JvmUrl = "https://cache-redirector.jetbrains.com/intellij-jbr/jbr_jcef-21.0.5-osx-x64-b631.28.tar.gz"
    windowsX64JvmUrl = "https://cache-redirector.jetbrains.com/intellij-jbr/jbr_jcef-21.0.5-windows-x64-b631.28.tar.gz"
}

dependencies {
    compileOnly(libs.bundles.toolbox.plugin.api)
    compileOnly(libs.bundles.serialization)
    compileOnly(libs.coroutines.core)
}

licenseReport {
    renderers = arrayOf(JsonReportRenderer("dependencies.json"))
    filters = arrayOf(ExcludeTransitiveDependenciesFilter())
    // jq script to convert to our format:
    // `jq '[.dependencies[] | {name: .moduleName, version: .moduleVersion, url: .moduleUrl, license: .moduleLicense, licenseUrl: .moduleLicenseUrl}]' < build/reports/dependency-license/dependencies.json > src/main/resources/dependencies.json`
}

tasks.compileKotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.jar {
    archiveBaseName.set(extension.id)
    dependsOn(extensionJson)
}

// region will be moved to the gradle plugin late
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


fun generateExtensionJson(extensionJson: ExtensionJson, destinationFile: Path) {
    val descriptor = ToolboxPluginDescriptor(
        id = extensionJson.id,
        version = extensionJson.version,
        apiVersion = libs.versions.toolbox.plugin.api.get(),
        meta = ToolboxMeta(
            name = extensionJson.meta.name,
            description = extensionJson.meta.description,
            vendor = extensionJson.meta.vendor,
            url = extensionJson.meta.url,
        )
    )
    val extensionJson = jacksonObjectMapper().writeValueAsString(descriptor)
    destinationFile.parent.createDirectories()
    destinationFile.writeText(extensionJson)
}

// endregion

val extension = ExtensionJson(
    id = "com.jetbrains.toolbox.sample",
    version = "1.0.0",
    meta = ExtensionJsonMeta(
        name = "Toolbox Sample Plugin",
        description = "Sample Plugin for JetBrains Toolbox",
        vendor = "JetBrains",
        url = "https://www.jetbrains.com/toolbox/",
    )
)

val extensionJsonFile = layout.buildDirectory.file("generated/extension.json")
val extensionJson by tasks.registering {
    inputs.property("extension", extension.toString())

    outputs.file(extensionJsonFile)
    doLast {
        generateExtensionJson(extension, extensionJsonFile.get().asFile.toPath())
    }
}

val copyPlugin by tasks.creating(Sync::class.java) {
    dependsOn(tasks.assemble)

    val userHome = System.getProperty("user.home").let { Path.of(it) }
    val toolboxCachesDir = when {
        SystemInfoRt.isWindows -> System.getenv("LOCALAPPDATA")?.let { Path.of(it) } ?: (userHome / "AppData" / "Local")
        // currently this is the location that TBA uses on Linux
        SystemInfoRt.isLinux -> System.getenv("XDG_DATA_HOME")?.let { Path.of(it) } ?: (userHome / ".local" / "share")
        SystemInfoRt.isMac -> userHome / "Library" / "Caches"
        else -> error("Unknown os")
    } / "JetBrains" / "Toolbox-Dev"

    val pluginsDir = when {
        SystemInfoRt.isWindows -> toolboxCachesDir / "cache"
        SystemInfoRt.isLinux || SystemInfoRt.isMac -> toolboxCachesDir
        else -> error("Unknown os")
    } / "plugins"

    val targetDir = pluginsDir / extension.id

    from(tasks.jar)

    from(extensionJsonFile)

    from("src/main/resources") {
        include("dependencies.json")
        include("icon.svg")
    }

    into(targetDir)

}

val pluginZip by tasks.registering(Zip::class) {
    dependsOn(tasks.assemble)
    dependsOn(tasks.getByName("generateLicenseReport"))

    from(tasks.assemble.get().outputs.files)
    from(extensionJsonFile)
    from("src/main/resources") {
        include("dependencies.json")
    }
    from("src/main/resources") {
        include("icon.svg")
        rename("icon.svg", "pluginIcon.svg")
    }
    archiveBaseName.set("${extension.id}-${extension.version}")
}


val toolboxPluginPropertiesFile = file("toolbox-plugin.properties")

val pluginMarketplaceToken: String = if (toolboxPluginPropertiesFile.exists()) {
    val token = Properties().apply { load(toolboxPluginPropertiesFile.inputStream()) }.getProperty("pluginMarketplaceToken", null)
    if (token == null) {
        error("pluginMarketplaceToken does not exist in ${toolboxPluginPropertiesFile.absolutePath}.\n" +
            "Please set pluginMarketplaceToken property to a token obtained from the marketplace.")
    }
    token
} else {
    error("toolbox-plugin.properties does not exist at ${toolboxPluginPropertiesFile.absolutePath}.\n" +
        "Please create the file and set pluginMarketplaceToken property to a token obtained from the marketplace.")
}

println("Plugin Marketplace Token: ${pluginMarketplaceToken.take(5)}*****")

// Work in progress. The public version of Marketplace will not accept the plugin yet
val uploadPlugin by tasks.registering {
    dependsOn(pluginZip)

    doLast {
        val instance = PluginRepositoryFactory.
        create(
            "https://plugins.jetbrains.com",
            pluginMarketplaceToken
        )

        // first upload
        instance.uploader.uploadNewPlugin(
            pluginZip.get().outputs.files.singleFile,
            listOf("toolbox", "gateway"),
            LicenseUrl.APACHE_2_0,
            ProductFamily.TOOLBOX,
            extension.meta.vendor,
            isHidden = true
        )

//        // subsequent updates
//        instance.uploader.upload(
//            pluginId,
//            pluginZip.outputs.files.singleFile
//        )
    }
}

gettext {
    potFile = project.layout.projectDirectory.file("src/main/resources/localization/defaultMessages.pot")
    keywords = listOf("ptrc:1c,2", "ptrl")
}