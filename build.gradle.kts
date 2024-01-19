group = "xyz.mcxross.ksui"

version = "1.3.1"

plugins {
  kotlin("jvm") apply false
  id("com.android.library") apply false
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
  id("org.jetbrains.dokka") version "1.9.10" apply false
}

repositories {
  mavenCentral()
  mavenLocal()
  google()
}
