import com.android.build.api.dsl.androidLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import kotlinx.rpc.protoc.proto
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.kotlinx.rpc)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.dokka)
  alias(libs.plugins.apollo.graphql)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotest)
}

group = "xyz.mcxross.ksui"

version = "2.2.6-SNAPSHOT"

kotlin {
  jvmToolchain(17)

  androidLibrary {
    namespace = "xyz.mcxross.ksui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

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
    val grpcMain by creating {
      dependsOn(commonMain.get())
      proto {
        include("google/protobuf/any.proto")
        include("google/protobuf/duration.proto")
        include("google/protobuf/empty.proto")
        include("google/protobuf/field_mask.proto")
        include("google/protobuf/struct.proto")
        include("google/protobuf/timestamp.proto")
        include("google/rpc/error_details.proto")
        include("google/rpc/status.proto")
        include("sui/rpc/v2/argument.proto")
        include("sui/rpc/v2/balance_change.proto")
        include("sui/rpc/v2/bcs.proto")
        include("sui/rpc/v2/checkpoint.proto")
        include("sui/rpc/v2/checkpoint_contents.proto")
        include("sui/rpc/v2/checkpoint_summary.proto")
        include("sui/rpc/v2/effects.proto")
        include("sui/rpc/v2/epoch.proto")
        include("sui/rpc/v2/error_reason.proto")
        include("sui/rpc/v2/event.proto")
        include("sui/rpc/v2/executed_transaction.proto")
        include("sui/rpc/v2/execution_status.proto")
        include("sui/rpc/v2/gas_cost_summary.proto")
        include("sui/rpc/v2/input.proto")
        include("sui/rpc/v2/jwk.proto")
        include("sui/rpc/v2/ledger_service.proto")
        include("sui/rpc/v2/move_package.proto")
        include("sui/rpc/v2/move_package_service.proto")
        include("sui/rpc/v2/name_service.proto")
        include("sui/rpc/v2/object.proto")
        include("sui/rpc/v2/object_reference.proto")
        include("sui/rpc/v2/owner.proto")
        include("sui/rpc/v2/protocol_config.proto")
        include("sui/rpc/v2/signature.proto")
        include("sui/rpc/v2/signature_scheme.proto")
        include("sui/rpc/v2/signature_verification_service.proto")
        include("sui/rpc/v2/state_service.proto")
        include("sui/rpc/v2/subscription_service.proto")
        include("sui/rpc/v2/system_state.proto")
        include("sui/rpc/v2/transaction.proto")
        include("sui/rpc/v2/transaction_execution_service.proto")
      }
      dependencies {
        api(libs.kotlinx.rpc.grpc.core)
        api(libs.kotlinx.rpc.protobuf.core)
        api(libs.kotlinx.rpc.grpc.client)
      }
    }
    val grpcTest by creating { dependsOn(commonTest.get()) }
    val grpcServerTest by creating {
      dependsOn(grpcTest)
      dependencies { implementation(libs.kotlinx.rpc.grpc.server) }
    }
    val androidJvmMain by creating {
      dependsOn(commonMain.get())
      dependencies { implementation(libs.bitcoinj.core) }
    }
    val appleMain by getting {
      dependsOn(grpcMain)
      dependencies { implementation(libs.ktor.client.darwin) }
    }
    val appleTest by getting { dependsOn(grpcTest) }
    val macosTest by getting { dependsOn(grpcServerTest) }
    val androidMain by getting {
      dependsOn(androidJvmMain)
      dependsOn(grpcMain)
      dependencies {
        implementation(libs.ktor.client.okhttp)
        implementation(libs.androidx.credentials)
        implementation(libs.androidx.credentials.play)
        implementation(libs.play.services.identity.credentials)
        implementation(libs.fastkrypto.android)
        implementation(libs.grpc.okhttp)
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
      dependsOn(grpcMain)
      dependencies {
        implementation(libs.ktor.client.cio)
        implementation(libs.logback.classic)
        implementation(libs.fastkrypto.jvm)
        implementation(libs.grpc.netty)
      }
    }
    val jvmTest by getting {
      dependsOn(grpcServerTest)
      dependencies {
        implementation(libs.kotest.runner.junit5)
        implementation(libs.kotlin.test.junit5)
      }
    }
    iosArm64Main.dependencies { implementation(libs.fastkrypto.iosarm64) }
    iosX64Main.dependencies { implementation(libs.fastkrypto.iosx64) }
    iosSimulatorArm64Main.dependencies { implementation(libs.fastkrypto.iossimulatorarm64) }
    macosArm64Main.dependencies { implementation(libs.fastkrypto.macosarm64) }
    macosX64Main.dependencies { implementation(libs.fastkrypto.macosx64) }
  }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

rpc {
  protoc {
    buf {
      generate {
        includeImports = false
        includeWkt = false
      }
    }
  }
}

tasks.matching { it.name == "bufGenerateJvmTest" }.configureEach { enabled = false }

apollo { service("service") { packageName.set("xyz.mcxross.ksui.generated") } }

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
