import org.gradle.kotlin.dsl.libs

plugins {
  kotlin("jvm")
  application
  alias(libs.plugins.kotlin.serialization)
}

group = "xyz.mcxross.ksui.sample"

kotlin { jvmToolchain(20) }

dependencies {
  implementation(project(":ksui"))
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.bcs)
  implementation(libs.ktor.client.cio)
  // The libraries no longer ship a logging backend; an application picks its own.
  runtimeOnly(libs.logback.classic)
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") { useJUnitPlatform() }

application { mainClass.set("xyz.mcxross.ksui.sample.MainKt") }
