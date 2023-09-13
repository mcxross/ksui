import java.net.URL
import java.util.*
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.signing
import org.jetbrains.dokka.gradle.DokkaTask

val ktorVersion: String = extra["ktor_version"] as String

plugins {
  kotlin("multiplatform")
  id("com.android.library")
  kotlin("plugin.serialization")
  id("org.jetbrains.dokka")
  id("maven-publish")
  id("signing")
}

group = "xyz.mcxross.ksui"

version = "1.3.1"

repositories {
  mavenCentral()
  mavenLocal()
  google()
}

ext["signing.keyId"] = null

ext["signing.password"] = null

ext["signing.secretKeyRingFile"] = null

ext["ossrhUsername"] = null

ext["ossrhPassword"] = null

kotlin {
  jvm {
    jvmToolchain(11)
    testRuns["test"].executionTask.configure { useJUnitPlatform() }
  }
  androidTarget { publishLibraryVariants("release", "debug") }
  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
    it.binaries.framework { baseName = "commonMain" }
  }
  js(IR) { browser() }
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
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
        //implementation("xyz.mcxross.bcs:bcs:1.0.0")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("io.ktor:ktor-client-mock:$ktorVersion")
      }
    }
    val androidMain by getting {
      dependencies { implementation("io.ktor:ktor-client-okhttp:$ktorVersion") }
    }
    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by creating {
      dependsOn(commonMain)
      iosX64Main.dependsOn(this)
      iosArm64Main.dependsOn(this)
      iosSimulatorArm64Main.dependsOn(this)
      dependencies { implementation("io.ktor:ktor-client-darwin:$ktorVersion") }
    }
    val iosX64Test by getting
    val iosArm64Test by getting
    val iosSimulatorArm64Test by getting
    val iosTest by creating {
      dependsOn(commonTest)
      iosX64Test.dependsOn(this)
      iosArm64Test.dependsOn(this)
      iosSimulatorArm64Test.dependsOn(this)
    }
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

android {
  compileSdk = 33
  defaultConfig {
    minSdk = 24
    targetSdk = 33
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  sourceSets {
    named("main") {
      manifest.srcFile("src/androidMain/AndroidManifest.xml")
      res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    }
  }
  buildTypes {
    getByName("release") { isMinifyEnabled = false }
    getByName("debug") {}
  }
  publishing {
    singleVariant("release") { withSourcesJar() }
    singleVariant("debug") { withSourcesJar() }
  }
}

val secretPropsFile = project.rootProject.file("local.properties")

if (secretPropsFile.exists()) {
  secretPropsFile
      .reader()
      .use { Properties().apply { load(it) } }
      .onEach { (name, value) -> ext[name.toString()] = value }
} else {
  ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
  ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
  ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_IN_MEMORY_SECRET_KEY")
  ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
  ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
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

val javadocJar =
    tasks.register<Jar>("javadocJar") {
      archiveClassifier.set("javadoc")
      dependsOn("dokkaHtml")
      from(buildDir.resolve("dokka"))
    }

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
  repositories {
    maven {
      name = "sonatype"
      setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = getExtraString("ossrhUsername")
        password = getExtraString("ossrhPassword")
      }
    }
  }

  publications.withType<MavenPublication> {
    artifact(javadocJar.get())

    pom {
      name.set("KMP Sui library")
      description.set(
          "Multiplatform Kotlin language JSON-RPC wrapper and crypto utilities for interacting with a Sui Full node.")
      url.set("https://github.com/mcxross")

      licenses {
        license {
          name.set("Apache License, Version 2.0")
          url.set("https://opensource.org/licenses/APACHE-2.0")
        }
      }
      developers {
        developer {
          id.set("mcxross")
          name.set("Mcxross")
          email.set("oss@mcxross.xyz")
        }
      }
      scm { url.set("https://github.com/mcxross/ksui") }
    }
  }
}

signing { sign(publishing.publications) }
