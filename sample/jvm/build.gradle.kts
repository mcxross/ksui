plugins {
  kotlin("jvm") version "1.8.10"
  application
}

group = "xyz.mcxross.ksui.sample"

version = "0.1.0"

repositories { mavenCentral() }

dependencies {
  implementation(project(":ksui"))
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") { useJUnitPlatform() }
