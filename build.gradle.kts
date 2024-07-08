group = "xyz.mcxross.ksui"

version = "2.1.0-SNAPSHOT"

plugins {
  kotlin("jvm") apply false
  id("com.android.library") apply false
  id("com.android.application") apply false
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
  id("org.jetbrains.dokka") version "1.9.10" apply false
  id("xyz.mcxross.graphql") version "0.1.0-beta06" apply false
  id("com.vanniktech.maven.publish") version "0.29.0" apply false
}

repositories {
  mavenCentral()
  mavenLocal()
  google()
}
