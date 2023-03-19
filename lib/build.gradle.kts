val ktor_version: String by project

plugins {
  kotlin("multiplatform") version "1.8.0"
  kotlin("plugin.serialization") version "1.8.0"
}

group = "xyz.mcxross.ksui"

version = "0.1.0"

repositories { mavenCentral() }

kotlin {
  jvm {
    jvmToolchain(8)
    withJava()
    testRuns["test"].executionTask.configure { useJUnitPlatform() }
  }
  js(BOTH) { browser { commonWebpackConfig { cssSupport { enabled.set(true) } } } }
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
        implementation("io.ktor:ktor-client-core:$ktor_version")
        implementation("io.ktor:ktor-client-cio:$ktor_version")
        implementation("io.ktor:ktor-client-websockets:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
      }
    }
    val commonTest by getting { dependencies { implementation(kotlin("test")) } }
    val jvmMain by getting
    val jvmTest by getting
    val jsMain by getting
    val jsTest by getting
    val nativeMain by getting
    val nativeTest by getting
  }
}
