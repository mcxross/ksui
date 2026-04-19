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
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotest)
}

group = "xyz.mcxross.ksui"

configurations.configureEach {
  exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
  exclude(group = "com.google.protobuf", module = "protobuf-javalite")
}

kotlin {
  jvmToolchain(17)

  androidLibrary {
    namespace = "xyz.mcxross.ksui.grpc"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  iosX64()
  iosArm64()
  iosSimulatorArm64()

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
    commonMain {
      proto {
        fileImports.from(fileTree("src/commonMain/proto") { include("google/protobuf/**/*.proto") })
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
        api(project(":ksui"))
        api(libs.kotlinx.rpc.grpc.core)
        api(libs.kotlinx.rpc.protobuf.core)
        api(libs.kotlinx.rpc.grpc.client)
      }
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotest.framework.engine)
      implementation(libs.kotest.assertions.core)
    }
    androidMain.dependencies { implementation(libs.grpc.okhttp) }
    jvmMain.dependencies {
      implementation(libs.grpc.netty)
      implementation(libs.logback.classic)
    }
    jvmTest.dependencies {
      implementation(libs.kotlinx.rpc.grpc.server)
      implementation(libs.kotest.runner.junit5)
      implementation(libs.kotlin.test.junit5)
    }
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

tasks.withType<DokkaTask>().configureEach {
  notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/2231")
}

dokka {
  moduleName.set("Ksui gRPC")
  dokkaPublications.html {
    suppressInheritedMembers.set(true)
    failOnWarning.set(true)
  }
  dokkaPublications.html { outputDirectory.set(layout.buildDirectory.dir("dokka")) }
  pluginsConfiguration.html { footerMessage.set("(c) McXross") }
}

mavenPublishing {
  coordinates("xyz.mcxross.ksui", "ksui-grpc", version.toString())

  configure(
    KotlinMultiplatform(
      javadocJar = JavadocJar.Dokka("dokkaGenerate"),
      sourcesJar = true,
      androidVariantsToPublish = listOf("debug", "release"),
    )
  )

  pom {
    name.set("Ksui gRPC")
    description.set("Multiplatform gRPC support for the Ksui SDK")
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
