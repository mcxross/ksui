plugins {
  kotlin("jvm")
  application
}

group = "xyz.mcxross.ksui.sample"

version = "1.3.1"

repositories {
  mavenLocal()
  mavenCentral()
}

kotlin { jvmToolchain(20) }

dependencies {
  implementation(project(":ksui"))
  implementation(libs.ktor.serialization.kotlinx.json)
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") { useJUnitPlatform() }

application { mainClass.set("xyz.mcxross.ksui.sample.MainKt") }
