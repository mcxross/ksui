pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }

  plugins {
    kotlin("jvm").version(extra["kotlin.version"] as String)
    kotlin("multiplatform").version(extra["kotlin.version"] as String)
    kotlin("plugin.serialization").version(extra["kotlin.version"] as String)
    kotlin("android").version(extra["kotlin.version"] as String)
    id("com.android.base").version(extra["agp.version"] as String)
    id("com.android.application").version(extra["agp.version"] as String)
    id("com.android.library").version(extra["agp.version"] as String)
    id("org.jetbrains.dokka").version(extra["kotlin.version"] as String)
  }

}

rootProject.name = "ksui"

include(":lib", ":sample:jvm")

project(":lib").name = "ksui"
