pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven(url = "../repo")
    mavenLocal()
  }

}

rootProject.name = "ksui"

include(":lib", ":sample:jvm", ":sample:android")

project(":lib").name = "ksui"
