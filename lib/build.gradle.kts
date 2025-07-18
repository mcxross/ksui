import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import java.net.URL
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.dokka)
  alias(libs.plugins.apollo.graphql)
  alias(libs.plugins.maven.publish)
}

group = "xyz.mcxross.ksui"

version = "2.2.0-SNAPSHOT"

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

  macosArm64()
  macosX64()
  tvosX64()
  tvosArm64()
  watchosArm32()
  watchosArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    val androidJvmMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.bitcoinj.core)
      }
    }
    appleMain.dependencies { implementation(libs.ktor.client.darwin) }
    val androidMain by getting {
      dependsOn(androidJvmMain)
      dependencies {
        implementation(libs.ktor.client.okhttp)
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
    }
    jsMain.dependencies { implementation(libs.ktor.client.js) }
    val jvmMain by getting {
      dependsOn(androidJvmMain)
      dependencies {
        implementation(libs.ktor.client.cio)
        implementation(libs.logback.classic)
      }
    }
    //linuxMain.dependencies { implementation(libs.ktor.client.curl) }
    //mingwMain.dependencies { implementation(libs.ktor.client.winhttp) }
  }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(20))

apollo {
  service("service") {
    packageName.set("xyz.mcxross.ksui.generated")
  }
}

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

mavenPublishing {
  coordinates("xyz.mcxross.ksui", "ksui", version.toString())

  configure(
    KotlinMultiplatform(
      javadocJar = JavadocJar.Dokka("dokkaHtml"),
      sourcesJar = true,
      androidVariantsToPublish = listOf("debug", "release"),
    ),
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
