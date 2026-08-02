pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven(url = "../repo")
    maven("https://redirector.kotlinlang.org/maven/kxrpc-grpc")
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" }

rootProject.name = "ksui"

include(":core", ":graphql", ":grpc", ":sample:jvm", ":sample:android")

project(":core").name = "ksui-core"

project(":graphql").name = "ksui"

project(":grpc").name = "ksui-grpc"

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
  repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven(url = "https://central.sonatype.com/repository/maven-snapshots")
    maven("https://redirector.kotlinlang.org/maven/kxrpc-grpc")
  }
}
