group = "xyz.mcxross.ksui"

version = "2.0.0-SNAPSHOT"

plugins {
  kotlin("jvm") apply false
  id("com.android.library") apply false
  id("com.android.application") apply false
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
  id("org.jetbrains.dokka") version "1.9.10" apply false
  id("xyz.mcxross.graphql") version "1.0.0-SNAPSHOT" apply false
}

repositories {
  mavenCentral()
  mavenLocal()
  google()
}
