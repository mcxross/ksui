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

kotlin { jvmToolchain(17) }

dependencies {
  implementation(project(":ksui"))
  implementation("org.bouncycastle:bcprov-jdk15on:1.70")
  implementation("org.bitcoinj:bitcoinj-core:0.16.1")
  implementation("org.apache.commons:commons-lang3:3.12.0")
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") { useJUnitPlatform() }

application { mainClass.set("xyz.mcxross.ksui.sample.MainKt") }
