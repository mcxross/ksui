import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

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
    extensions.configure<YarnRootExtension> {
      // Keep Kotlin/JS tooling on Dependabot's patched dependency releases.
      resolution("**/karma/lodash", "4.18.1")
      resolution("**/karma-webpack/webpack-merge/lodash", "4.18.1")
      resolution("**/mocha/glob", "10.5.0")
      resolution("**/ws", "8.21.0")
      resolution("**/karma/log4js/flatted", "3.4.2")
      resolution("**/karma/body-parser", "1.20.6")
      resolution("**/karma-webpack/minimatch", "9.0.7")
      resolution("**/mocha/minimatch", "9.0.7")
      resolution("**/karma/socket.io/socket.io-parser", "4.2.7")
      resolution("**/karma/socket.io/engine.io", "6.6.7")
      resolution("**/karma/braces", "3.0.3")
      resolution("**/webpack-cli/cross-spawn", "7.0.5")
      resolution("**/mocha/serialize-javascript", "7.0.5")
      resolution("**/webpack/terser-webpack-plugin/serialize-javascript", "7.0.5")
      resolution("**/karma/tmp", "0.2.6")
      resolution("**/webpack/schema-utils/ajv/fast-uri", "3.1.5")
      resolution("fast-uri", "3.1.5")
      resolution("**/mocha/js-yaml", "4.3.1")
      resolution("**/karma/chokidar/anymatch/picomatch", "2.3.2")
      resolution("**/karma/chokidar/readdirp/picomatch", "2.3.2")
      resolution("**/karma/chokidar/braces", "3.0.3")
      resolution("**/karma/http-proxy/follow-redirects", "1.16.0")
      resolution("**/karma/body-parser/qs", "6.14.2")
      resolution("qs", "6.15.3")
      resolution("**/webpack/schema-utils/ajv", "8.18.0")
      resolution("**/webpack/schema-utils/ajv-formats/ajv", "8.18.0")
      resolution("webpack", "5.104.1")
      resolution("**/mocha/diff", "8.0.3")
    }
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
