import java.net.URL
import org.jetbrains.dokka.gradle.DokkaTask

val ktorVersion: String = extra["ktor_version"] as String

plugins {
  kotlin("multiplatform") version "1.8.10"
  kotlin("plugin.serialization") version "1.8.10"
  id("org.jetbrains.dokka") version "1.8.10"
  id("maven-publish")
}

group = "xyz.mcxross.ksui"

version = "0.29.0-beta.1"

repositories { mavenCentral() }

kotlin {
  jvm {
    jvmToolchain(8)
    withJava()
    testRuns["test"].executionTask.configure { useJUnitPlatform() }
  }
  js(IR) { browser { commonWebpackConfig { cssSupport { enabled.set(true) } } } }
  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget =
    when {
      hostOs == "Mac OS X" -> macosX64("native")
      hostOs == "Linux" -> linuxX64("native")
      isMingwX64 -> mingwX64("native")
      else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-core:$ktorVersion")
        implementation("io.ktor:ktor-client-websockets:$ktorVersion")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
      }
    }
    val commonTest by getting { dependencies { implementation(kotlin("test")) } }
    val jvmMain by getting {
      dependencies { implementation("io.ktor:ktor-client-cio:$ktorVersion") }
    }
    val jvmTest by getting
    val jsMain by getting { dependencies { implementation("io.ktor:ktor-client-js:$ktorVersion") } }
    val jsTest by getting
    val nativeMain by getting {
      dependencies { implementation("io.ktor:ktor-client-curl:$ktorVersion") }
    }
    val nativeTest by getting
  }
}

tasks.getByName<DokkaTask>("dokkaHtml") {
  moduleName.set("Ksui")
  outputDirectory.set(file(buildDir.resolve("dokka")))
  dokkaSourceSets {
    configureEach {
      includes.from("Module.md")
      sourceLink {
        localDirectory.set(file("commonMain/kotlin"))
        remoteUrl.set(URL("https://github.com/mcxross/ksui/blob/master/lib/src/commonMain/kotlin"))
        remoteLineSuffix.set("#L")
      }
    }
  }
}
