import org.gradle.internal.impldep.org.junit.Ignore

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

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
  }
}
