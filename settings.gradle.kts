pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven(url = "../repo")
    mavenLocal()
  }

  plugins {
    kotlin("jvm").version(extra["kotlin.version"] as String)
    kotlin("multiplatform").version(extra["kotlin.version"] as String)
    kotlin("plugin.serialization").version(extra["kotlin.version"] as String)
    kotlin("android").version(extra["kotlin.version"] as String)
    id("com.android.base").version(extra["agp.version"] as String)
    id("com.android.application").version(extra["agp.version"] as String)
    id("com.android.library").version(extra["agp.version"] as String)
    id("org.jetbrains.dokka").version("1.9.20")
  }
}

rootProject.name = "ksui"

include(":lib", ":sample:jvm", ":sample:android")

project(":lib").name = "ksui"
