import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootEnvSpec

group = "xyz.mcxross.ksui"

plugins {
  alias(libs.plugins.jvm) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlinx.rpc) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.apollo.graphql) apply false
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotest) apply false
}

allprojects {
  plugins.withType<NodeJsPlugin> {
    extensions.configure<NodeJsEnvSpec> { download.set(false) }
  }
  plugins.withType<YarnPlugin> {
    extensions.configure<YarnRootEnvSpec> { download.set(false) }
  }
}

val dokkaSiteDir = layout.buildDirectory.dir("dokka-site")
val dokkaSiteIndexDir = layout.buildDirectory.dir("dokka-site-index")

val generateDokkaSiteIndex by tasks.registering {
  val indexFile = dokkaSiteIndexDir.map { it.file("index.html") }
  outputs.file(indexFile)

  doLast {
    indexFile
      .get()
      .asFile
      .writeText(
        """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Ksui API Documentation</title>
            <style>
              body {
                margin: 0;
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                color: #1f2933;
                background: #f7f9fb;
              }
              main {
                max-width: 880px;
                margin: 0 auto;
                padding: 48px 24px;
              }
              h1 {
                margin: 0 0 12px;
                font-size: 36px;
                font-weight: 700;
              }
              p {
                margin: 0 0 28px;
                color: #52606d;
                line-height: 1.6;
              }
              ul {
                display: grid;
                gap: 12px;
                padding: 0;
                margin: 0;
                list-style: none;
              }
              a {
                display: block;
                padding: 16px 18px;
                border: 1px solid #d9e2ec;
                border-radius: 8px;
                color: #102a43;
                background: #ffffff;
                text-decoration: none;
              }
              a:hover {
                border-color: #627d98;
              }
            </style>
          </head>
          <body>
            <main>
              <h1>Ksui API Documentation</h1>
              <p>Select a module. Each section is generated from the module that owns that API surface.</p>
              <ul>
                <li><a href="core/">Ksui Core</a></li>
                <li><a href="graphql/">Ksui GraphQL</a></li>
                <li><a href="grpc/">Ksui gRPC</a></li>
              </ul>
            </main>
          </body>
        </html>
        """
          .trimIndent()
      )
  }
}

tasks.register<Sync>("assembleDokkaSite") {
  dependsOn(
    ":ksui-core:dokkaGenerate",
    ":ksui:dokkaGenerate",
    ":ksui-grpc:dokkaGenerate",
    generateDokkaSiteIndex,
  )

  into(dokkaSiteDir)
  from(dokkaSiteIndexDir)
  from(project(":ksui-core").layout.buildDirectory.dir("dokka")) { into("core") }
  from(project(":ksui").layout.buildDirectory.dir("dokka")) { into("graphql") }
  from(project(":ksui-grpc").layout.buildDirectory.dir("dokka")) { into("grpc") }
}
