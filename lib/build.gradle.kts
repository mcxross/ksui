import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.dokka)
  alias(libs.plugins.apollo.graphql)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotest)
}

group = "xyz.mcxross.ksui"

version = "2.3.6-SNAPSHOT"

repositories {
  maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
  mavenCentral()
  mavenLocal()
  google()
}

kotlin {
  jvmToolchain(17)

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
  }
  jvm {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    testRuns["test"].executionTask.configure { useJUnitPlatform() }
  }

  macosArm64()
  macosX64()
  tvosX64()
  tvosArm64()
  watchosArm32()
  watchosArm64()

  applyDefaultHierarchyTemplate()

  targets.configureEach {
    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }
      }
    }
  }

  sourceSets {
    val androidJvmMain by creating {
      dependsOn(commonMain.get())
      dependencies { implementation(libs.bitcoinj.core) }
    }
    appleMain.dependencies { implementation(libs.ktor.client.darwin) }
    val androidMain by getting {
      dependsOn(androidJvmMain)
      dependencies {
        implementation(libs.ktor.client.okhttp)
        implementation(libs.androidx.credentials)
        implementation(libs.androidx.credentials.play)
        implementation(libs.play.services.identity.credentials)
        implementation(libs.fastcrypto.android)
      }
    }
    commonMain.dependencies {
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.client.websockets)
      implementation(libs.ktor.client.logging)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.bcs)
      implementation(libs.apollo.runtime)
      implementation(libs.kotlin.result)
    }
    commonTest.dependencies {
      implementation(libs.ktor.client.mock)
      implementation(libs.kotlin.test)
      implementation(libs.kotest.framework.engine)
      implementation(libs.kotest.assertions.core)
    }
    jsMain.dependencies { implementation(libs.ktor.client.js) }
    val jvmMain by getting {
      dependsOn(androidJvmMain)
      dependencies {
        implementation(libs.ktor.client.cio)
        implementation(libs.logback.classic)
        implementation(libs.fastcrypto.jvm)
      }
    }
    jvmTest.dependencies { implementation("io.kotest:kotest-runner-junit5:6.0.7") }
    iosArm64Main.dependencies { implementation(libs.fastcrypto.iosarm64) }
    iosX64Main.dependencies { implementation(libs.fastcrypto.iosx64) }
    iosSimulatorArm64Main.dependencies { implementation(libs.fastcrypto.iossimulatorarm64) }
    macosArm64Main.dependencies { implementation(libs.fastcrypto.macosarm64) }
    macosX64Main.dependencies { implementation(libs.fastcrypto.macosx64) }
  }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

apollo { service("service") { packageName.set("xyz.mcxross.ksui.generated") } }

android {
  namespace = "xyz.mcxross.ksui"
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

tasks.withType<DokkaTask>().configureEach {
  notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/2231")
}

dokka {
  moduleName.set("Ksui")
  dokkaPublications.html {
    suppressInheritedMembers.set(true)
    failOnWarning.set(true)
  }
  dokkaSourceSets {
    configureEach {
      includes.from("Module.md")
      sourceLink {
        localDirectory.set(file("commonMain/kotlin"))
        remoteUrl("https://github.com/mcxross/ksui/blob/master/lib/src/commonMain/kotlin")
        remoteLineSuffix.set("#L")
      }
    }
  }
  dokkaPublications.html { outputDirectory.set(layout.buildDirectory.dir("dokka")) }

  pluginsConfiguration.html { footerMessage.set("(c) McXross") }
}

mavenPublishing {
  coordinates("xyz.mcxross.ksui", "ksui", version.toString())

  configure(
    KotlinMultiplatform(
      javadocJar = JavadocJar.Dokka("dokkaGenerate"),
      sourcesJar = true,
      androidVariantsToPublish = listOf("debug", "release"),
    )
  )

  pom {
    name.set("Ksui")
    description.set("Multiplatform SDK for the SUI blockchain")
    inceptionYear.set("2023")
    url.set("https://github.com/mcxross")
    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
      }
    }
    developers {
      developer {
        id.set("mcxross")
        name.set("Mcxross")
        email.set("oss@mcxross.xyz")
        url.set("https://mcxross.xyz/")
      }
    }
    scm {
      url.set("https://github.com/mcxross/ksui")
      connection.set("scm:git:ssh://github.com/mcxross/ksui.git")
      developerConnection.set("scm:git:ssh://github.com/mcxross/ksui.git")
    }
  }

  publishToMavenCentral(automaticRelease = true)

  signAllPublications()
}
