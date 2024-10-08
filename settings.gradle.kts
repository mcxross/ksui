pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven(url = "../repo")
    mavenLocal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" }

rootProject.name = "ksui"

include(":lib", ":sample:jvm", ":sample:android")

project(":lib").name = "ksui"
