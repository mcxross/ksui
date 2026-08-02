import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.dokka)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotest)
}

group = "xyz.mcxross.ksui"

kotlin {
  jvmToolchain(17)

  android {
    namespace = "xyz.mcxross.ksui.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  js {
    browser { testTask { useKarma { useChromeHeadless() } } }
    nodejs()
  }

  jvm {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    testRuns["test"].executionTask.configure { useJUnitPlatform() }
  }

  macosArm64()
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
      dependencies { implementation(libs.bouncycastle.bcprov) }
    }
    val androidMain by getting {
      dependsOn(androidJvmMain)
      dependencies {
        implementation(libs.androidx.credentials)
        implementation(libs.androidx.credentials.play)
        implementation(libs.play.services.identity.credentials)
        implementation(libs.fastkrypto.android)
      }
    }
    commonMain.dependencies {
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.bcs)
      implementation(libs.kotlin.result)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotest.framework.engine)
      implementation(libs.kotest.assertions.core)
    }
    jsMain.dependencies {
      implementation(npm("@noble/curves", "2.2.0"))
      implementation(npm("@noble/hashes", "2.2.0"))
      implementation(npm("@scure/bip39", "2.2.0"))
    }
    val jvmMain by getting {
      dependsOn(androidJvmMain)
      dependencies {
        implementation(libs.logback.classic)
        implementation(libs.fastkrypto.jvm)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.runner.junit5)
        implementation(libs.kotlin.test.junit5)
      }
    }
    iosArm64Main.dependencies { implementation(libs.fastkrypto.iosarm64) }
    iosX64Main.dependencies { implementation(libs.fastkrypto.iosx64) }
    iosSimulatorArm64Main.dependencies { implementation(libs.fastkrypto.iossimulatorarm64) }
    macosArm64Main.dependencies { implementation(libs.fastkrypto.macosarm64) }
  }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

tasks.withType<Test>().configureEach {
  testLogging {
    events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    exceptionFormat = TestExceptionFormat.FULL
  }
}

tasks.withType<DokkaTask>().configureEach {
  notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/2231")
}

dokka {
  moduleName.set("Ksui core")
  dokkaPublications.html {
    suppressInheritedMembers.set(true)
    failOnWarning.set(true)
  }
  dokkaPublications.html { outputDirectory.set(layout.buildDirectory.dir("dokka")) }
  pluginsConfiguration.html { footerMessage.set("(c) McXross") }
}

mavenPublishing {
  coordinates("xyz.mcxross.ksui", "ksui-core", version.toString())

  configure(
    KotlinMultiplatform(
      javadocJar = JavadocJar.Dokka("dokkaGenerate"),
      sourcesJar = true,
      androidVariantsToPublish = listOf("debug", "release"),
    )
  )

  pom {
    name.set("Ksui core")
    description.set("Shared models, accounts, cryptography, and transaction building for Ksui")
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
