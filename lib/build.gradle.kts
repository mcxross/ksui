import java.net.URL
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.signing
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
  kotlin("multiplatform")
  id("com.android.library")
  kotlin("plugin.serialization")
  id("org.jetbrains.dokka")
  id("maven-publish")
  id("signing")
}

group = "xyz.mcxross.ksui"

version = "1.3.2"

repositories {
  maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
  mavenCentral()
  mavenLocal()
  google()
}

kotlin {
  androidTarget { publishLibraryVariants("release", "debug") }

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  js {
    browser {
      testTask {
        useKarma {
          useChromeHeadless()
          useSafari()
        }
      }
    }
    nodejs()
    compilations.all {
      kotlinOptions.sourceMap = true
      kotlinOptions.moduleKind = "umd"
    }
  }
  jvm { testRuns["test"].executionTask.configure { useJUnitPlatform() } }

  linuxX64()
  linuxArm64()
  macosArm64()
  macosX64()
  tvosX64()
  tvosArm64()
  watchosX64()
  watchosArm32()
  watchosArm64()
  mingwX64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.client.websockets)
      implementation(libs.ktor.client.logging)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.bcs)
      implementation(libs.kase64)
    }

    commonTest.dependencies {
      implementation(libs.ktor.client.mock)
      implementation(libs.kotlin.test)
    }

    jsMain.dependencies { implementation(libs.ktor.client.js) }

    jvmMain.dependencies { implementation(libs.ktor.client.cio) }

    androidMain.dependencies { implementation(libs.ktor.client.okhttp) }

    iosMain.dependencies { implementation(libs.ktor.client.darwin) }
  }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

android {
  namespace = "mcxross.ksui"
  defaultConfig {
    minSdk = 24
    compileSdk = 33
  }

  sourceSets {
    named("main") {
      manifest.srcFile("src/androidMain/AndroidManifest.xml")
      res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    }
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

tasks.withType<DokkaTask>().configureEach {
  notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/2231")
}

val javadocJar =
  tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    dependsOn("dokkaHtml")
    from(buildDir.resolve("dokka"))
  }

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
  if (hasProperty("sonatypeUser") && hasProperty("sonatypePass")) {
    repositories {
      maven {
        name = "sonatype"
        val isSnapshot = version.toString().endsWith("-SNAPSHOT")
        setUrl(
          if (isSnapshot) {
            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
          } else {
            "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
          }
        )
        credentials {
          username = property("sonatypeUser") as String
          password = property("sonatypePass") as String
        }
      }
    }
  }

  publications.withType<MavenPublication> {
    artifact(javadocJar.get())

    pom {
      name.set("KMP Sui library")
      description.set(
        "Multiplatform Kotlin language JSON-RPC wrapper and crypto utilities for interacting with a Sui Full node."
      )
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

signing {
  val sonatypeGpgKey = System.getenv("SONATYPE_GPG_KEY")
  val sonatypeGpgKeyPassword = System.getenv("SONATYPE_GPG_KEY_PASSWORD")
  when {
    sonatypeGpgKey == null || sonatypeGpgKeyPassword == null -> useGpgCmd()
    else -> useInMemoryPgpKeys(sonatypeGpgKey, sonatypeGpgKeyPassword)
  }
  // sign(publishing.publications)
}
