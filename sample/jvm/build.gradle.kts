import org.gradle.kotlin.dsl.libs

plugins {
  kotlin("jvm")
  application
  alias(libs.plugins.kotlin.serialization)
}

group = "xyz.mcxross.ksui.sample"

version = "1.3.1"

kotlin { jvmToolchain(20) }

dependencies {
  implementation(project(":ksui"))
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.apollo.runtime)
  implementation(libs.bcs)
  implementation(libs.ktor.client.cio)
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
}

sourceSets["main"].kotlin.srcDir("../../lib/build/generated/source/apollo/service")

tasks.getByName<Test>("test") { useJUnitPlatform() }

application { mainClass.set("xyz.mcxross.ksui.sample.MainKt") }
