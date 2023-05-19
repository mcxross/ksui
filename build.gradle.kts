group = "xyz.mcxross.ksui"

version = "1.2.0-beta"

plugins {
  kotlin("jvm") version "1.8.10" apply false
  kotlin("multiplatform") version "1.8.10" apply false
  kotlin("plugin.serialization") version "1.8.10" apply false
  id("org.jetbrains.dokka") version "1.8.10" apply false
}

repositories { mavenCentral() }
