pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    // Pre-release kotlinx-rpc gRPC builds (0.11.0-grpc-*) are published only here.
    maven("https://redirector.kotlinlang.org/maven/kxrpc-grpc") {
      content { includeGroupAndSubgroups("org.jetbrains.kotlinx") }
    }
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" }

rootProject.name = "ksui"

include(":core", ":graphql", ":grpc", ":suikit", ":sample:jvm")

project(":core").name = "ksui-core"

project(":graphql").name = "ksui"

project(":grpc").name = "ksui-grpc"

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
  // No mavenLocal(): a locally installed artifact with the same version would silently replace the
  // published one, so local builds could test code that users never get.
  repositories {
    mavenCentral()
    google()
    // Pre-release kotlinx-rpc gRPC builds (0.11.0-grpc-*) are published only here.
    maven("https://redirector.kotlinlang.org/maven/kxrpc-grpc") {
      content { includeGroupAndSubgroups("org.jetbrains.kotlinx") }
    }
  }
}
