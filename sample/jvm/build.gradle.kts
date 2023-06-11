import org.jetbrains.kotlin.cli.jvm.main

plugins {
  kotlin("jvm")
  application
}

group = "xyz.mcxross.ksui.sample"

version = "1.2.2-beta"

repositories { mavenCentral() }

dependencies {
  implementation(project(":ksui"))
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") { useJUnitPlatform() }

application {
  mainClass.set("xyz.mcxross.ksui.sample.MainKt")
}
