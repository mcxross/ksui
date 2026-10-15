// swift-tools-version:5.9
import PackageDescription

// Set by `./gradlew :suikit:writeSuiKitReleaseManifest` when the Release SuiKit workflow publishes.
let release: (url: String, checksum: String)? = (url: "https://github.com/mcxross/ksui/releases/download/v3.1.0-SNAPSHOT.137/SuiKit.xcframework.zip", checksum: "bdc91736a66fb523b71c0c1845e3fb1ddff328b4335e572dced253e6e2e73c73")

// SUIKIT_LOCAL_BUILD, or an unreleased checkout, uses `./gradlew :suikit:assembleSuiKitReleaseXCFramework`.
let suiKit: Target =
  if let release, Context.environment["SUIKIT_LOCAL_BUILD"] == nil {
    .binaryTarget(name: "SuiKit", url: release.url, checksum: release.checksum)
  } else {
    .binaryTarget(name: "SuiKit", path: "suikit/build/XCFrameworks/release/SuiKit.xcframework")
  }

let package = Package(
  name: "ksui",
  platforms: [.iOS(.v15), .macOS(.v12)],
  products: [.library(name: "SuiKit", targets: ["SuiKit"])],
  targets: [suiKit]
)
