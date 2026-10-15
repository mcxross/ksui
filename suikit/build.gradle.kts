import java.security.MessageDigest
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins { alias(libs.plugins.kotlin.multiplatform) }

group = "xyz.mcxross.ksui"

val suiKitInfoSources = layout.buildDirectory.dir("generated/suikit/kotlin")

// A framework needs at least one compiled source, so SuiKit carries its own version.
val generateSuiKitInfo by
  tasks.registering {
    description = "Generates the SuiKit version object."
    val output = suiKitInfoSources
    val sdkVersion = project.version.toString()
    inputs.property("version", sdkVersion)
    outputs.dir(output)
    doLast {
      val file = output.get().file("xyz/mcxross/ksui/suikit/SuiKitInfo.kt").asFile
      file.parentFile.mkdirs()
      file.writeText(
        """
        package xyz.mcxross.ksui.suikit

        object SuiKitInfo {
          const val VERSION: String = "$sdkVersion"
        }

        """
          .trimIndent()
      )
    }
  }

kotlin {
  val xcFramework = XCFramework("SuiKit")
  // iosX64 is the Intel simulator slice; generic simulator builds link it too.
  listOf(iosArm64(), iosSimulatorArm64(), iosX64(), macosArm64()).forEach { target ->
    target.binaries.framework {
      baseName = "SuiKit"
      // Two static Kotlin frameworks cannot be linked into one app, so SuiKit stays dynamic.
      isStatic = false
      export(project(":ksui-core"))
      export(project(":ksui"))
      xcFramework.add(this)
    }
  }

  sourceSets {
    commonMain {
      kotlin.srcDir(generateSuiKitInfo)
      dependencies {
        api(project(":ksui-core"))
        api(project(":ksui"))
      }
    }
  }
}

val xcFrameworkDirectory = layout.buildDirectory.dir("XCFrameworks/release")

val distributions = layout.buildDirectory.dir("distributions")

// Gradle's Copy and Zip tasks follow symlinks, which would break the macOS framework slice.
val zipSuiKitXCFramework by
  tasks.registering(Exec::class) {
    group = "distribution"
    description = "Archives the release SuiKit XCFramework and, separately, its debug symbols."
    dependsOn("assembleSuiKitReleaseXCFramework")
    val output = distributions.get().asFile
    inputs.dir(xcFrameworkDirectory)
    outputs.files(output.resolve("SuiKit.xcframework.zip"), output.resolve("SuiKit.dSYMs.zip"))
    commandLine(
      "bash",
      file("archive-suikit.sh").absolutePath,
      xcFrameworkDirectory.get().file("SuiKit.xcframework").asFile.absolutePath,
      output.absolutePath,
    )
  }

tasks.register("writeSuiKitReleaseManifest") {
  group = "distribution"
  description = "Points the root Package.swift at the published SuiKit archive (-PsuiKitReleaseUrl)."
  dependsOn(zipSuiKitXCFramework)
  val archive = distributions.map { it.file("SuiKit.xcframework.zip") }
  val manifest = rootProject.layout.projectDirectory.file("Package.swift").asFile
  val releaseUrl = providers.gradleProperty("suiKitReleaseUrl")
  doLast {
    val url = releaseUrl.orNull ?: throw GradleException("Set -PsuiKitReleaseUrl to the archive URL")
    // SwiftPM's binary-target checksum is the archive's SHA-256.
    val digest = MessageDigest.getInstance("SHA-256").digest(archive.get().asFile.readBytes())
    val checksum = digest.joinToString("") { "%02x".format(it) }
    val declaration =
      Regex("""^let release: \(url: String, checksum: String\)\? = .*$""", RegexOption.MULTILINE)
    val text = manifest.readText()
    check(declaration.containsMatchIn(text)) { "Package.swift has no release declaration" }
    manifest.writeText(
      text.replace(declaration) {
        """let release: (url: String, checksum: String)? = (url: "$url", checksum: "$checksum")"""
      }
    )
    logger.lifecycle("SuiKit $url $checksum")
  }
}
