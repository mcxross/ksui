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

val swiftPackageManifest = layout.buildDirectory.file("swiftpm-manifest/Package.swift")

val writeSuiKitSwiftPackageManifest by
  tasks.registering {
    description = "Writes the manifest of the local SuiKit Swift package."
    val manifest = swiftPackageManifest
    outputs.file(manifest)
    doLast {
      manifest
        .get()
        .asFile
        .writeText(
          """
          // swift-tools-version:5.9
          import PackageDescription

          let package = Package(
            name: "SuiKit",
            platforms: [.iOS(.v15), .macOS(.v12)],
            products: [.library(name: "SuiKit", targets: ["SuiKit"])],
            targets: [.binaryTarget(name: "SuiKit", path: "SuiKit.xcframework")]
          )

          """
            .trimIndent()
        )
    }
  }

tasks.register<Sync>("assembleSuiKitSwiftPackage") {
  group = "build"
  description = "Assembles a local Swift package that wraps the release SuiKit XCFramework."
  dependsOn("assembleSuiKitReleaseXCFramework")
  from(layout.buildDirectory.dir("XCFrameworks/release")) { include("SuiKit.xcframework/**") }
  from(writeSuiKitSwiftPackageManifest)
  into(layout.buildDirectory.dir("swiftpm/SuiKit"))
}
