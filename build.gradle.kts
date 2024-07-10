group = "xyz.mcxross.ksui"

version = "2.1.0-SNAPSHOT"

plugins {
  alias(libs.plugins.jvm) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.graphql.multiplatform) apply false
  alias(libs.plugins.maven.publish) apply false
}

repositories {
  mavenCentral()
  mavenLocal()
  google()
}
